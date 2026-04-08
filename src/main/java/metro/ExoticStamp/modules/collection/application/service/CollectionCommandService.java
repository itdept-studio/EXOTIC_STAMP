package metro.ExoticStamp.modules.collection.application.service;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.collection.application.command.CollectStampCommand;
import metro.ExoticStamp.modules.collection.application.port.UserStampCachePort;
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
import metro.ExoticStamp.modules.collection.presentation.response.ProgressResponse;
import metro.ExoticStamp.modules.collection.presentation.response.StampCollectResponse;
import metro.ExoticStamp.modules.metro.application.StationQueryService;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationDetailResponse;
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
    private final StationQueryService stationQueryService;
    private final CollectionQueryService collectionQueryService;
    private final UserStampCachePort cachePort;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public StampCollectResponse collectStamp(CollectStampCommand cmd) {
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

        StationDetailResponse station = hasNfc
                ? stationQueryService.resolveStationByNfc(cmd.nfcTagId())
                : stationQueryService.resolveStationByQr(cmd.qrToken());

        Campaign campaign = resolveCampaign(station.getLineId(), cmd.campaignId());

        domainService.assertNotAlreadyCollected(cmd.userId(), station.getId(), campaign.getId());

        StampDesign design = stampDesignRepository.findActiveByCampaignIdAndStationId(campaign.getId(), station.getId())
                .orElseThrow(() -> new InvalidStationException(station.getId(), "No active stamp design configured for campaign"));

        LocalDateTime now = LocalDateTime.now();
        UserStamp toSave = UserStamp.builder()
                .userId(cmd.userId())
                .stationId(station.getId())
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
        UUID lineId = station.getLineId();
        cachePort.evictUserStamps(cmd.userId(), lineId);
        cachePort.evictUserProgress(cmd.userId(), lineId);

        ProgressResponse progress = collectionQueryService.computeProgress(cmd.userId(), lineId, campaign.getId());

        RbacTransactionCallbacks.afterCommit(() -> eventPublisher.publishEvent(new StampCollectedEvent(
                UUID.randomUUID(),
                cmd.userId(),
                station.getId(),
                lineId,
                campaign.getId(),
                saved.getCollectedAt(),
                collectMethod
        )));

        return StampCollectResponse.builder()
                .stampId(saved.getId())
                .stationId(station.getId())
                .stationName(station.getName())
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

    private StampCollectResponse buildResponse(UserStamp userStamp, boolean isNew) {
        StationDetailResponse station = stationQueryService.getStationDetailById(userStamp.getStationId());
        StampDesign design = stampDesignRepository.findById(userStamp.getStampDesignId()).orElse(null);
        ProgressResponse progress = collectionQueryService.computeProgress(userStamp.getUserId(), station.getLineId(), userStamp.getCampaignId());
        return StampCollectResponse.builder()
                .stampId(userStamp.getId())
                .stationId(station.getId())
                .stationName(station.getName())
                .lineId(station.getLineId())
                .campaignId(userStamp.getCampaignId())
                .stampDesignUrl(design != null ? design.getArtworkUrl() : null)
                .collectedAt(userStamp.getCollectedAt())
                .isNew(isNew)
                .collectMethod(userStamp.getCollectMethod() != null ? userStamp.getCollectMethod().name() : null)
                .progress(progress)
                .build();
    }
}

