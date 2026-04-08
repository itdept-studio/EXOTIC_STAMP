package metro.ExoticStamp.modules.collection.application.service;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.collection.application.command.CollectStampCommand;
import metro.ExoticStamp.modules.collection.application.port.UserStampCachePort;
import metro.ExoticStamp.modules.collection.application.view.ProgressView;
import metro.ExoticStamp.modules.collection.application.view.StampCollectView;
import metro.ExoticStamp.modules.collection.domain.event.StampCollectedEvent;
import metro.ExoticStamp.modules.collection.domain.exception.CampaignNotFoundException;
import metro.ExoticStamp.modules.collection.domain.exception.InvalidStationException;
import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.model.CollectMethod;
import metro.ExoticStamp.modules.collection.domain.model.StampDesign;
import metro.ExoticStamp.modules.collection.domain.model.UserStamp;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignRepository;
import metro.ExoticStamp.modules.collection.domain.repository.StampDesignRepository;
import metro.ExoticStamp.modules.collection.domain.repository.UserStampRepository;
import metro.ExoticStamp.modules.collection.domain.service.CollectionDomainService;
import metro.ExoticStamp.modules.metro.application.port.StationReadPort;
import metro.ExoticStamp.modules.metro.application.view.MetroStationView;
import metro.ExoticStamp.modules.rbac.application.support.RbacTransactionCallbacks;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CollectionCommandService {

    private final CampaignRepository campaignRepository;
    private final StampDesignRepository stampDesignRepository;
    private final UserStampRepository userStampRepository;
    private final CollectionDomainService domainService;
    private final StationReadPort stationReadPort;
    private final CollectionQueryService collectionQueryService;
    private final UserStampCachePort cachePort;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public StampCollectView collectStamp(CollectStampCommand cmd) {
        if (cmd == null) throw new IllegalArgumentException("Missing command");
        if (cmd.userId() == null) throw new IllegalArgumentException("Missing userId");
        if (cmd.idempotencyKey() == null) throw new IllegalArgumentException("Missing idempotencyKey");
        boolean hasNfc = cmd.nfcTagId() != null && !cmd.nfcTagId().isBlank();
        boolean hasQr = cmd.qrToken() != null && !cmd.qrToken().isBlank();
        if (!hasNfc && !hasQr) throw new IllegalArgumentException("Either nfcTagId or qrToken is required");
        if (hasNfc && hasQr) throw new IllegalArgumentException("Provide only one of nfcTagId or qrToken");

        UserStamp existing = userStampRepository.findByIdempotencyKey(cmd.idempotencyKey().toString()).orElse(null);
        if (existing != null) {
            if (!cmd.userId().equals(existing.getUserId())) {
                throw new IllegalArgumentException("Idempotency key already used by another user");
            }
            return buildResponse(existing, false);
        }

        CollectMethod collectMethod = cmd.collectMethod() != null
                ? cmd.collectMethod()
                : (hasNfc ? CollectMethod.NFC : CollectMethod.QR);

        MetroStationView station = hasNfc
                ? stationReadPort.resolveStationViewByNfc(cmd.nfcTagId())
                : stationReadPort.resolveStationViewByQr(cmd.qrToken());

        Campaign campaign = resolveCampaign(station.lineId(), cmd.campaignId());

        domainService.assertNotAlreadyCollected(cmd.userId(), station.id(), campaign.getId());

        StampDesign design = stampDesignRepository.findActiveByCampaignIdAndStationId(campaign.getId(), station.id())
                .orElseThrow(() -> new InvalidStationException(station.id(), "No active stamp design configured for campaign"));

        LocalDateTime now = LocalDateTime.now();
        UserStamp toSave = UserStamp.builder()
                .userId(cmd.userId())
                .stationId(station.id())
                .campaignId(campaign.getId())
                .stampDesignId(design.getId())
                .collectedAt(now)
                .latitude(cmd.latitude())
                .longitude(cmd.longitude())
                .gpsVerified(false)
                .collectMethod(collectMethod)
                .deviceFingerprint(cmd.deviceFingerprint())
                .idempotencyKey(cmd.idempotencyKey().toString())
                .createdAt(now)
                .build();

        UserStamp saved = userStampRepository.save(toSave);

        // Evict caches after write (done in transaction; actual Redis delete is best-effort)
        UUID lineId = station.lineId();
        cachePort.evictUserStamps(cmd.userId(), lineId);
        cachePort.evictUserProgress(cmd.userId(), lineId);

        ProgressView progress = collectionQueryService.computeProgress(cmd.userId(), lineId, campaign.getId());

        RbacTransactionCallbacks.afterCommit(() -> eventPublisher.publishEvent(new StampCollectedEvent(
                UUID.randomUUID(),
                cmd.userId(),
                station.id(),
                lineId,
                campaign.getId(),
                saved.getCollectedAt(),
                collectMethod
        )));

        return StampCollectView.builder()
                .stampId(saved.getId())
                .stationId(station.id())
                .stationName(station.name())
                .lineId(lineId)
                .campaignId(campaign.getId())
                .stampDesignUrl(design.getArtworkUrl())
                .collectedAt(saved.getCollectedAt())
                .isNew(true)
                .collectMethod(collectMethod.name())
                .progress(progress)
                .build();
    }

    private Campaign resolveCampaign(UUID lineId, UUID campaignId) {
        if (campaignId != null) {
            return campaignRepository.findById(campaignId).orElseThrow(() -> new CampaignNotFoundException(campaignId));
        }
        return campaignRepository.findDefaultByLineId(lineId)
                .orElseThrow(() -> new CampaignNotFoundException(null));
    }

    private StampCollectView buildResponse(UserStamp userStamp, boolean isNew) {
        MetroStationView station = stationReadPort.getStationViewById(userStamp.getStationId());
        StampDesign design = stampDesignRepository.findById(userStamp.getStampDesignId()).orElse(null);
        ProgressView progress = collectionQueryService.computeProgress(userStamp.getUserId(), station.lineId(), userStamp.getCampaignId());
        return StampCollectView.builder()
                .stampId(userStamp.getId())
                .stationId(station.id())
                .stationName(station.name())
                .lineId(station.lineId())
                .campaignId(userStamp.getCampaignId())
                .stampDesignUrl(design != null ? design.getArtworkUrl() : null)
                .collectedAt(userStamp.getCollectedAt())
                .isNew(isNew)
                .collectMethod(userStamp.getCollectMethod() != null ? userStamp.getCollectMethod().name() : null)
                .progress(progress)
                .build();
    }
}

