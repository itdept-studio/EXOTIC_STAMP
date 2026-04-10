package metro.ExoticStamp.modules.collection.application;

import metro.ExoticStamp.modules.collection.application.command.CollectStampCommand;
import metro.ExoticStamp.modules.collection.application.port.UserStampCachePort;
import metro.ExoticStamp.modules.collection.application.service.CollectionCommandService;
import metro.ExoticStamp.modules.collection.application.service.CollectionQueryService;
import metro.ExoticStamp.modules.collection.application.view.ProgressView;
import metro.ExoticStamp.modules.collection.application.view.StampCollectView;
import metro.ExoticStamp.modules.collection.config.CollectionProperties;
import metro.ExoticStamp.modules.collection.domain.event.StampCollectedEvent;
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
import metro.ExoticStamp.modules.metro.domain.exception.StationInactiveException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectionCommandServiceTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID LINE_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID STATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000011");
    private static final UUID CAMPAIGN_ID = UUID.fromString("00000000-0000-0000-0000-000000000020");
    private static final UUID DESIGN_ID = UUID.fromString("00000000-0000-0000-0000-000000000030");
    private static final UUID STAMP_ID = UUID.fromString("00000000-0000-0000-0000-000000000040");

    @Mock private CampaignRepository campaignRepository;
    @Mock private StampDesignRepository stampDesignRepository;
    @Mock private UserStampRepository userStampRepository;
    @Mock private CollectionDomainService domainService;
    @Mock private StationReadPort stationReadPort;
    @Mock private CollectionQueryService collectionQueryService;
    @Mock private UserStampCachePort cachePort;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private CollectionProperties collectionProperties;

    private Clock clock;
    private CollectionCommandService service;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2025-06-01T12:00:00Z"), ZoneOffset.UTC);
        when(collectionProperties.getIdempotencyWindow()).thenReturn(Duration.ofHours(1));
        service = new CollectionCommandService(
                campaignRepository,
                stampDesignRepository,
                userStampRepository,
                domainService,
                stationReadPort,
                collectionQueryService,
                cachePort,
                eventPublisher,
                collectionProperties,
                clock
        );
    }

    @Test
    void collect_newStamp_success() {
        UUID idempotencyKey = UUID.fromString("00000000-0000-0000-0000-000000000999");

        when(domainService.resolveIdempotentStamp(eq(idempotencyKey.toString()), eq(USER_ID), any())).thenReturn(Optional.empty());
        when(stationReadPort.resolveStationViewByNfc("NFC1"))
                .thenReturn(MetroStationView.builder().id(STATION_ID).lineId(LINE_ID).name("Central").sequence(1).active(true).build());
        Campaign campaign = Campaign.builder()
                .lineId(LINE_ID)
                .isDefault(true)
                .isActive(true)
                .code("DEF")
                .name("C")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .build();
        campaign.setId(CAMPAIGN_ID);
        when(campaignRepository.findDefaultByLineId(LINE_ID)).thenReturn(Optional.of(campaign));

        StampDesign design = StampDesign.builder().campaignId(CAMPAIGN_ID).stationId(STATION_ID).name("S").artworkUrl("https://cdn/x.png").isActive(true).isLimited(false).build();
        design.setId(DESIGN_ID);
        when(stampDesignRepository.findActiveByCampaignIdAndStationId(eq(CAMPAIGN_ID), eq(STATION_ID)))
                .thenReturn(Optional.of(design));

        when(userStampRepository.save(any(UserStamp.class))).thenAnswer(inv -> {
            UserStamp us = inv.getArgument(0);
            us.setId(STAMP_ID);
            return us;
        });

        when(collectionQueryService.computeProgress(USER_ID, LINE_ID, CAMPAIGN_ID))
                .thenReturn(ProgressView.builder().lineId(LINE_ID).collected(1).total(10).percentage(10).build());

        CollectStampCommand cmd = new CollectStampCommand(
                USER_ID,
                idempotencyKey,
                "NFC1",
                null,
                null,
                "device-fingerprint-123",
                null,
                null,
                CollectMethod.NFC
        );

        StampCollectView res = service.collectStamp(cmd);

        assertNotNull(res.stampId());
        assertTrue(res.isNew());
        assertEquals("Central", res.stationName());
        verify(cachePort).evictAllForUserCollection(USER_ID, LINE_ID, CAMPAIGN_ID);

        ArgumentCaptor<StampCollectedEvent> cap = ArgumentCaptor.forClass(StampCollectedEvent.class);
        verify(eventPublisher).publishEvent(cap.capture());
        assertEquals(USER_ID, cap.getValue().getUserId());
        assertEquals(STATION_ID, cap.getValue().getStationId());
        assertEquals(LINE_ID, cap.getValue().getLineId());
        assertEquals(CAMPAIGN_ID, cap.getValue().getCampaignId());
        assertEquals(CollectMethod.NFC, cap.getValue().getCollectMethod());
    }

    @Test
    void collect_duplicate_throwsConflict() {
        UUID idempotencyKey = UUID.randomUUID();
        when(domainService.resolveIdempotentStamp(eq(idempotencyKey.toString()), eq(USER_ID), any())).thenReturn(Optional.empty());
        when(stationReadPort.resolveStationViewByQr("QR1"))
                .thenReturn(MetroStationView.builder().id(STATION_ID).lineId(LINE_ID).name("Central").sequence(1).active(true).build());

        Campaign c = Campaign.builder().lineId(LINE_ID).isDefault(true).isActive(true).code("DEF").name("C").startDate(LocalDateTime.now()).endDate(LocalDateTime.now().plusDays(1)).build();
        c.setId(CAMPAIGN_ID);
        when(campaignRepository.findDefaultByLineId(LINE_ID)).thenReturn(Optional.of(c));

        doThrow(new StampAlreadyCollectedException(STATION_ID))
                .when(domainService).assertNotAlreadyCollected(USER_ID, STATION_ID, CAMPAIGN_ID);

        CollectStampCommand cmd = new CollectStampCommand(
                USER_ID,
                idempotencyKey,
                null,
                "QR1",
                null,
                "device-fingerprint-123",
                null,
                null,
                CollectMethod.QR
        );

        assertThrows(StampAlreadyCollectedException.class, () -> service.collectStamp(cmd));
        verify(userStampRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void collect_inactiveStation_rejected() {
        UUID idempotencyKey = UUID.randomUUID();
        when(domainService.resolveIdempotentStamp(eq(idempotencyKey.toString()), eq(USER_ID), any())).thenReturn(Optional.empty());
        when(stationReadPort.resolveStationViewByNfc("NFC_INACTIVE"))
                .thenThrow(new StationInactiveException(STATION_ID));

        CollectStampCommand cmd = new CollectStampCommand(
                USER_ID,
                idempotencyKey,
                "NFC_INACTIVE",
                null,
                null,
                "device-fingerprint-123",
                null,
                null,
                CollectMethod.NFC
        );

        assertThrows(StationInactiveException.class, () -> service.collectStamp(cmd));
        verify(userStampRepository, never()).save(any());
    }

    @Test
    void collect_idempotencyKey_returnsExisting() {
        UUID idempotencyKey = UUID.randomUUID();
        UserStamp existing = UserStamp.builder()
                .userId(USER_ID)
                .stationId(STATION_ID)
                .campaignId(CAMPAIGN_ID)
                .stampDesignId(DESIGN_ID)
                .collectedAt(LocalDateTime.now(clock).minusMinutes(1))
                .gpsVerified(false)
                .collectMethod(CollectMethod.QR)
                .deviceFingerprint("device-fingerprint-123")
                .idempotencyKey(idempotencyKey.toString())
                .build();
        existing.setId(STAMP_ID);

        when(domainService.resolveIdempotentStamp(eq(idempotencyKey.toString()), eq(USER_ID), any())).thenReturn(Optional.of(existing));
        when(stationReadPort.getStationViewById(STATION_ID))
                .thenReturn(MetroStationView.builder().id(STATION_ID).lineId(LINE_ID).name("Central").sequence(1).active(true).build());
        when(stampDesignRepository.findById(DESIGN_ID))
                .thenReturn(Optional.of(StampDesign.builder().artworkUrl("https://cdn/x.png").name("S").isActive(true).isLimited(false).build()));
        when(collectionQueryService.computeProgress(USER_ID, LINE_ID, CAMPAIGN_ID))
                .thenReturn(ProgressView.builder().lineId(LINE_ID).collected(1).total(10).percentage(10).build());

        CollectStampCommand cmd = new CollectStampCommand(
                USER_ID,
                idempotencyKey,
                null,
                "QR1",
                null,
                "device-fingerprint-123",
                null,
                null,
                CollectMethod.QR
        );

        StampCollectView res = service.collectStamp(cmd);
        assertFalse(res.isNew());
        assertEquals(STAMP_ID, res.stampId());
        verify(userStampRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void collect_dataIntegrity_mapsToStampAlreadyCollected() {
        UUID idempotencyKey = UUID.randomUUID();
        when(domainService.resolveIdempotentStamp(eq(idempotencyKey.toString()), eq(USER_ID), any())).thenReturn(Optional.empty());
        when(stationReadPort.resolveStationViewByNfc("NFC1"))
                .thenReturn(MetroStationView.builder().id(STATION_ID).lineId(LINE_ID).name("Central").sequence(1).active(true).build());
        Campaign campaign = Campaign.builder()
                .lineId(LINE_ID).isDefault(true).isActive(true).code("DEF").name("C")
                .startDate(LocalDateTime.now()).endDate(LocalDateTime.now().plusDays(1)).build();
        campaign.setId(CAMPAIGN_ID);
        when(campaignRepository.findDefaultByLineId(LINE_ID)).thenReturn(Optional.of(campaign));
        StampDesign design = StampDesign.builder().campaignId(CAMPAIGN_ID).stationId(STATION_ID).name("S").artworkUrl("https://cdn/x.png").isActive(true).isLimited(false).build();
        design.setId(DESIGN_ID);
        when(stampDesignRepository.findActiveByCampaignIdAndStationId(CAMPAIGN_ID, STATION_ID)).thenReturn(Optional.of(design));

        when(userStampRepository.save(any(UserStamp.class))).thenThrow(
                new DataIntegrityViolationException("duplicate", new RuntimeException("uq_user_stamps_collect")));

        CollectStampCommand cmd = new CollectStampCommand(
                USER_ID,
                idempotencyKey,
                "NFC1",
                null,
                null,
                "fp",
                null,
                null,
                CollectMethod.NFC
        );

        assertThrows(StampAlreadyCollectedException.class, () -> service.collectStamp(cmd));
    }

    @Test
    void collect_eventPublishFailure_stillSucceeds() {
        UUID idempotencyKey = UUID.randomUUID();
        when(domainService.resolveIdempotentStamp(anyString(), eq(USER_ID), any())).thenReturn(Optional.empty());
        when(stationReadPort.resolveStationViewByNfc("NFC1"))
                .thenReturn(MetroStationView.builder().id(STATION_ID).lineId(LINE_ID).name("Central").sequence(1).active(true).build());
        Campaign campaign = Campaign.builder()
                .lineId(LINE_ID).isDefault(true).isActive(true).code("DEF").name("C")
                .startDate(LocalDateTime.now()).endDate(LocalDateTime.now().plusDays(1)).build();
        campaign.setId(CAMPAIGN_ID);
        when(campaignRepository.findDefaultByLineId(LINE_ID)).thenReturn(Optional.of(campaign));
        StampDesign design = StampDesign.builder().campaignId(CAMPAIGN_ID).stationId(STATION_ID).name("S").artworkUrl("u").isActive(true).isLimited(false).build();
        design.setId(DESIGN_ID);
        when(stampDesignRepository.findActiveByCampaignIdAndStationId(CAMPAIGN_ID, STATION_ID)).thenReturn(Optional.of(design));
        when(userStampRepository.save(any(UserStamp.class))).thenAnswer(inv -> {
            UserStamp us = inv.getArgument(0);
            us.setId(STAMP_ID);
            return us;
        });
        when(collectionQueryService.computeProgress(USER_ID, LINE_ID, CAMPAIGN_ID))
                .thenReturn(ProgressView.builder().lineId(LINE_ID).collected(1).total(10).percentage(10).build());

        doThrow(new RuntimeException("broker down")).when(eventPublisher).publishEvent(any(ApplicationEvent.class));

        CollectStampCommand cmd = new CollectStampCommand(
                USER_ID,
                idempotencyKey,
                "NFC1",
                null,
                null,
                "fp",
                null,
                null,
                CollectMethod.NFC
        );

        assertDoesNotThrow(() -> service.collectStamp(cmd));
    }
}
