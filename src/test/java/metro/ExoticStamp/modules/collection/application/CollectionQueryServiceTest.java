package metro.ExoticStamp.modules.collection.application;

import metro.ExoticStamp.modules.collection.application.mapper.UserStampAppMapper;
import metro.ExoticStamp.modules.collection.application.port.UserStampCachePort;
import metro.ExoticStamp.modules.collection.application.service.CollectionQueryService;
import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.model.CollectMethod;
import metro.ExoticStamp.modules.collection.domain.model.StampDesign;
import metro.ExoticStamp.modules.collection.domain.model.UserStamp;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignRepository;
import metro.ExoticStamp.modules.collection.domain.repository.StampDesignRepository;
import metro.ExoticStamp.modules.collection.domain.repository.UserStampRepository;
import metro.ExoticStamp.modules.collection.presentation.response.ProgressResponse;
import metro.ExoticStamp.modules.collection.presentation.response.StampBookResponse;
import metro.ExoticStamp.modules.collection.presentation.response.UserStampResponse;
import metro.ExoticStamp.modules.metro.application.LineQueryService;
import metro.ExoticStamp.modules.metro.application.StationQueryService;
import metro.ExoticStamp.modules.metro.presentation.dto.response.LineDetailResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationDetailResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationResponse;
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
import static org.mockito.ArgumentMatchers.any;
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
    @Mock private StationQueryService stationQueryService;
    @Mock private LineQueryService lineQueryService;
    @Mock private UserStampCachePort cachePort;

    private CollectionQueryService service;

    @BeforeEach
    void setUp() {
        service = new CollectionQueryService(
                campaignRepository,
                stampDesignRepository,
                userStampRepository,
                stationQueryService,
                lineQueryService,
                cachePort,
                new UserStampAppMapper()
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
        when(lineQueryService.getLineDetail(LINE_ID, true)).thenReturn(LineDetailResponse.builder()
                .id(LINE_ID)
                .stations(List.of(
                        StationResponse.builder().id(UUID.randomUUID()).build(),
                        StationResponse.builder().id(UUID.randomUUID()).build(),
                        StationResponse.builder().id(UUID.randomUUID()).build(),
                        StationResponse.builder().id(UUID.randomUUID()).build(),
                        StationResponse.builder().id(UUID.randomUUID()).build(),
                        StationResponse.builder().id(UUID.randomUUID()).build(),
                        StationResponse.builder().id(UUID.randomUUID()).build(),
                        StationResponse.builder().id(UUID.randomUUID()).build(),
                        StationResponse.builder().id(UUID.randomUUID()).build(),
                        StationResponse.builder().id(UUID.randomUUID()).build()
                ))
                .build());

        ProgressResponse res = service.getMyProgress(USER_ID, LINE_ID, null);
        assertEquals(5L, res.getCollected());
        assertEquals(10L, res.getTotal());
        assertEquals(50, res.getPercentage());
        verify(cachePort).putUserProgress(eq(USER_ID), eq(LINE_ID), any());
    }

    @Test
    void stampBookQuery_returnsGrid() {
        Campaign c = Campaign.builder().lineId(LINE_ID).isDefault(true).isActive(true).code("DEF").name("C")
                .startDate(LocalDateTime.now()).endDate(LocalDateTime.now().plusDays(1)).build();
        c.setId(CAMPAIGN_ID);
        when(campaignRepository.findDefaultByLineId(LINE_ID)).thenReturn(Optional.of(c));

        when(lineQueryService.getLineDetail(LINE_ID, true)).thenReturn(LineDetailResponse.builder()
                .id(LINE_ID)
                .stations(List.of(
                        StationResponse.builder().id(STATION_ID).name("Central").sequence(1).build(),
                        StationResponse.builder().id(UUID.fromString("00000000-0000-0000-0000-000000000012")).name("Next").sequence(2).build()
                ))
                .build());

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

        when(stampDesignRepository.findActiveByCampaignIdAndStationId(eq(CAMPAIGN_ID), any(UUID.class)))
                .thenAnswer(inv -> {
                    UUID stationId = inv.getArgument(1);
                    if (STATION_ID.equals(stationId)) {
                        return Optional.of(StampDesign.builder()
                                .name("S")
                                .artworkUrl("https://cdn/central.png")
                                .isActive(true)
                                .isLimited(false)
                                .build());
                    }
                    return Optional.empty();
                });

        StampBookResponse res = service.getStampBook(USER_ID, LINE_ID, null);
        assertEquals(2, res.getStations().size());
        assertTrue(res.getStations().get(0).isCollected());
        assertFalse(res.getStations().get(1).isCollected());
    }

    @Test
    void historyQuery_returnsRecent() {
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
        when(userStampRepository.findRecentByUserId(USER_ID, 20)).thenReturn(List.of(us));
        when(stationQueryService.getStationDetailById(STATION_ID))
                .thenReturn(StationDetailResponse.builder().id(STATION_ID).lineId(LINE_ID).name("Central").isActive(true).build());
        when(stampDesignRepository.findById(DESIGN_ID))
                .thenReturn(Optional.of(StampDesign.builder().name("S").artworkUrl("https://cdn/x.png").isActive(true).isLimited(false).build()));

        List<UserStampResponse> res = service.getMyHistory(USER_ID, 20);
        assertEquals(1, res.size());
        assertEquals("Central", res.get(0).getStationName());
    }
}

