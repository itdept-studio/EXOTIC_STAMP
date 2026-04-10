package metro.ExoticStamp.modules.collection.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.modules.collection.application.command.CollectStampCommand;
import metro.ExoticStamp.modules.collection.application.port.UserStampCachePort;
import metro.ExoticStamp.modules.collection.application.view.ProgressView;
import metro.ExoticStamp.modules.collection.application.view.StampCollectView;
import metro.ExoticStamp.modules.collection.config.CollectionProperties;
import metro.ExoticStamp.modules.collection.domain.event.StampCollectedEvent;
import metro.ExoticStamp.modules.collection.domain.exception.CampaignNotFoundException;
import metro.ExoticStamp.modules.collection.domain.exception.InvalidRequestException;
import metro.ExoticStamp.modules.collection.domain.exception.InvalidStationException;
import metro.ExoticStamp.modules.collection.domain.exception.StampAlreadyCollectedException;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
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
    private final CollectionProperties collectionProperties;
    private final Clock clock;

    /**
     * Resolves NFC/QR scan, enforces idempotency window, persists stamp, evicts caches, publishes event after commit.
     */
    @Transactional
    public StampCollectView collectStamp(CollectStampCommand cmd) {
        if (cmd == null) {
            throw new InvalidRequestException("Missing command");
        }
        if (cmd.userId() == null) {
            throw new InvalidRequestException("Missing userId");
        }
        if (cmd.idempotencyKey() == null) {
            throw new InvalidRequestException("Missing idempotencyKey");
        }
        boolean hasNfc = cmd.nfcTagId() != null && !cmd.nfcTagId().isBlank();
        boolean hasQr = cmd.qrToken() != null && !cmd.qrToken().isBlank();
        if (!hasNfc && !hasQr) {
            throw new InvalidRequestException("Either nfcTagId or qrToken is required");
        }
        if (hasNfc && hasQr) {
            throw new InvalidRequestException("Provide only one of nfcTagId or qrToken");
        }

        String idempotencyKeyStr = cmd.idempotencyKey().toString();
        LocalDateTime since = LocalDateTime.now(clock).minus(collectionProperties.getIdempotencyWindow());
        Optional<UserStamp> idempotent = domainService.resolveIdempotentStamp(idempotencyKeyStr, cmd.userId(), since);
        if (idempotent.isPresent()) {
            return buildResponse(idempotent.get(), false);
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

        LocalDateTime now = LocalDateTime.now(clock);
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
                .idempotencyKey(idempotencyKeyStr)
                .createdAt(now)
                .build();

        UserStamp saved;
        try {
            saved = userStampRepository.save(toSave);
        } catch (DataIntegrityViolationException ex) {
            if (isUserStampCollectUniqueViolation(ex)) {
                throw new StampAlreadyCollectedException(station.id());
            }
            throw ex;
        }

        UUID lineId = station.lineId();
        cachePort.evictAllForUserCollection(cmd.userId(), lineId, campaign.getId());

        ProgressView progress = collectionQueryService.computeProgress(cmd.userId(), lineId, campaign.getId());

        RbacTransactionCallbacks.afterCommit(() -> {
            try {
                eventPublisher.publishEvent(new StampCollectedEvent(
                        this,
                        UUID.randomUUID(),
                        cmd.userId(),
                        station.id(),
                        lineId,
                        campaign.getId(),
                        saved.getCollectedAt(),
                        collectMethod
                ));
            } catch (Exception e) {
                log.error("[Collection] StampCollectedEvent publish failed userId={} stationId={}: {}",
                        cmd.userId(), station.id(), e.getMessage(), e);
            }
        });

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

    private static boolean isUserStampCollectUniqueViolation(DataIntegrityViolationException ex) {
        String msg = ex.getMostSpecificCause().getMessage();
        if (msg == null) {
            return false;
        }
        return msg.contains("uq_user_stamps_collect")
                || (msg.contains("user_stamps") && msg.contains("user_id") && msg.contains("station_id"));
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
        ProgressView progress = collectionQueryService.computeProgress(
                userStamp.getUserId(), station.lineId(), userStamp.getCampaignId());
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
