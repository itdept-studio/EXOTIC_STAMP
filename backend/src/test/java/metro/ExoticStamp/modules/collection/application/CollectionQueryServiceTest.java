package metro.ExoticStamp.modules.collection.application;

import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.modules.collection.application.mapper.UserStampAppMapper;
import metro.ExoticStamp.modules.collection.application.port.UserStampCachePort;
import metro.ExoticStamp.modules.collection.application.service.CollectionQueryService;
import metro.ExoticStamp.modules.collection.application.view.ProgressView;
import metro.ExoticStamp.modules.collection.application.view.StampBookView;
import metro.ExoticStamp.modules.collection.application.view.UserStampView;
import metro.ExoticStamp.modules.collection.config.CollectionProperties;
import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.model.CollectMethod;
import metro.ExoticStamp.modules.collection.domain.model.StampDesign;
import metro.ExoticStamp.modules.collection.domain.model.UserStamp;
import metro.ExoticStamp.modules.collection.domain.model.UserStampSlice;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignRepository;
import metro.ExoticStamp.modules.collection.domain.repository.StampDesignRepository;
import metro.ExoticStamp.modules.collection.domain.repository.UserStampRepository;
import metro.ExoticStamp.modules.metro.application.port.StationReadPort;
import metro.ExoticStamp.modules.metro.application.view.MetroStationView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectionQueryServiceTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID LINE_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID STATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000011");
    private static final UUID CAMPAIGN_ID = UUID.fromString("00000000-0000-0000-0000-000000000020");
    private static final UUID DESIGN_ID = UUID.fromString("00000000-0000-0000-0000-000000000030");

    @Mock private CampaignRepository campaignRepository;
    @Mock private StampDesignRepository stampDesignRepository;
    @Mock private UserStampRepository userStampRepository;
    @Mock private StationReadPort stationReadPort;
    @Mock private UserStampCachePort cachePort;
    @Mock private CollectionProperties collectionProperties;

    private CollectionQueryService service;

    @BeforeEach
    void setUp() {
        lenient().when(collectionProperties.getDefaultPageSize()).thenReturn(20);
        lenient().when(collectionProperties.getMaxPageSize()).thenReturn(50);
        service = new CollectionQueryService(
                campaignRepository,
                stampDesignRepository,
                userStampRepository,
                stationReadPort,
                cachePort,
                new UserStampAppMapper(),
                collectionProperties
        );
    }

    @Test
    void progressQuery_computesCorrectly() {
        Campaign c = Campaign.builder().lineId(LINE_ID).isDefault(true).isActive(true).code("DEF").name("C")
                .startDate(LocalDateTime.now()).endDate(LocalDateTime.now().plusDays(1)).build();
        c.setId(CAMPAIGN_ID);
        when(campaignRepository.findDefaultByLineId(LINE_ID)).thenReturn(Optional.of(c));
        when(cachePort.getUserProgress(USER_ID, LINE_ID)).thenReturn(Optional.empty());
        when(userStampRepository.countDistinctStationsByUserIdAndCampaignId(USER_ID, CAMPAIGN_ID)).thenReturn(5L);
        when(stationReadPort.listActiveStationsByLineId(LINE_ID)).thenReturn(List.of(
                MetroStationView.builder().id(UUID.randomUUID()).lineId(LINE_ID).name("S1").sequence(1).active(true).build(),
                MetroStationView.builder().id(UUID.randomUUID()).lineId(LINE_ID).name("S2").sequence(2).active(true).build(),
                MetroStationView.builder().id(UUID.randomUUID()).lineId(LINE_ID).name("S3").sequence(3).active(true).build(),
                MetroStationView.builder().id(UUID.randomUUID()).lineId(LINE_ID).name("S4").sequence(4).active(true).build(),
                MetroStationView.builder().id(UUID.randomUUID()).lineId(LINE_ID).name("S5").sequence(5).active(true).build(),
                MetroStationView.builder().id(UUID.randomUUID()).lineId(LINE_ID).name("S6").sequence(6).active(true).build(),
                MetroStationView.builder().id(UUID.randomUUID()).lineId(LINE_ID).name("S7").sequence(7).active(true).build(),
                MetroStationView.builder().id(UUID.randomUUID()).lineId(LINE_ID).name("S8").sequence(8).active(true).build(),
                MetroStationView.builder().id(UUID.randomUUID()).lineId(LINE_ID).name("S9").sequence(9).active(true).build(),
                MetroStationView.builder().id(UUID.randomUUID()).lineId(LINE_ID).name("S10").sequence(10).active(true).build()
        ));

        ProgressView res = service.getMyProgress(USER_ID, LINE_ID, null);
        assertEquals(5L, res.collected());
        assertEquals(10L, res.total());
        assertEquals(50, res.percentage());
        verify(cachePort).putUserProgress(eq(USER_ID), eq(LINE_ID), any());
    }

    @Test
    void stampBookQuery_returnsGrid() {
        Campaign c = Campaign.builder().lineId(LINE_ID).isDefault(true).isActive(true).code("DEF").name("C")
                .startDate(LocalDateTime.now()).endDate(LocalDateTime.now().plusDays(1)).build();
        c.setId(CAMPAIGN_ID);
        when(campaignRepository.findDefaultByLineId(LINE_ID)).thenReturn(Optional.of(c));
        when(cachePort.getStampBook(USER_ID, LINE_ID, CAMPAIGN_ID)).thenReturn(Optional.empty());

        when(stationReadPort.listActiveStationsByLineId(LINE_ID)).thenReturn(List.of(
                MetroStationView.builder().id(STATION_ID).lineId(LINE_ID).name("Central").sequence(1).active(true).build(),
                MetroStationView.builder().id(UUID.fromString("00000000-0000-0000-0000-000000000012")).lineId(LINE_ID).name("Next").sequence(2).active(true).build()
        ));

        UserStamp us = UserStamp.builder()
                .userId(USER_ID)
                .stationId(STATION_ID)
                .campaignId(CAMPAIGN_ID)
                .stampDesignId(DESIGN_ID)
                .collectedAt(LocalDateTime.now())
                .gpsVerified(false)
                .collectMethod(CollectMethod.NFC)
                .deviceFingerprint("device-fingerprint-123")
                .idempotencyKey(UUID.randomUUID().toString())
                .build();
        when(userStampRepository.findByUserIdAndCampaignId(USER_ID, CAMPAIGN_ID)).thenReturn(List.of(us));

        when(stampDesignRepository.findActiveByCampaignIdAndStationIdIn(eq(CAMPAIGN_ID), anyList()))
                .thenReturn(List.of(StampDesign.builder()
                        .stationId(STATION_ID)
                        .campaignId(CAMPAIGN_ID)
                        .name("S")
                        .artworkUrl("https://cdn/central.png")
                        .isActive(true)
                        .isLimited(false)
                        .build()));

        StampBookView res = service.getStampBook(USER_ID, LINE_ID, null);
        assertEquals(2, res.stations().size());
        assertTrue(res.stations().get(0).collected());
        assertFalse(res.stations().get(1).collected());
        verify(cachePort).putStampBook(eq(USER_ID), eq(LINE_ID), eq(CAMPAIGN_ID), any());
    }

    @Test
    void historyQuery_returnsRecentPage() {
        UserStamp us = UserStamp.builder()
                .userId(USER_ID)
                .stationId(STATION_ID)
                .campaignId(CAMPAIGN_ID)
                .stampDesignId(DESIGN_ID)
                .collectedAt(LocalDateTime.now())
                .gpsVerified(false)
                .collectMethod(CollectMethod.QR)
                .deviceFingerprint("device-fingerprint-123")
                .idempotencyKey(UUID.randomUUID().toString())
                .build();
        when(cachePort.getUserHistory(USER_ID, 0, 20)).thenReturn(Optional.empty());
        when(userStampRepository.findByUserIdPaged(USER_ID, 0, 20))
                .thenReturn(new UserStampSlice(List.of(us), 1, 1, 0, 20));
        when(stationReadPort.listStationViewsByIds(any())).thenReturn(List.of(
                MetroStationView.builder().id(STATION_ID).lineId(LINE_ID).name("Central").sequence(1).active(true).build()));
        StampDesign sd = StampDesign.builder().name("S").artworkUrl("https://cdn/x.png").isActive(true).isLimited(false).build();
        sd.setId(DESIGN_ID);
        when(stampDesignRepository.findAllByIdIn(any())).thenReturn(List.of(sd));

        PageResponse<UserStampView> res = service.getMyHistory(USER_ID, 0, 20);
        assertEquals(1, res.content().size());
        assertEquals("Central", res.content().get(0).stationName());
        verify(cachePort).putUserHistory(eq(USER_ID), eq(0), eq(20), any());
    }
}
