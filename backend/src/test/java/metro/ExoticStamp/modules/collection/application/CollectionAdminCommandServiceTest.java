package metro.ExoticStamp.modules.collection.application;

import metro.ExoticStamp.modules.collection.application.command.AdminCreateStampDesignCommand;
import metro.ExoticStamp.modules.collection.application.mapper.CollectionAdminMapper;
import metro.ExoticStamp.modules.collection.application.service.CollectionAdminCommandService;
import metro.ExoticStamp.modules.collection.domain.exception.InvalidStationException;
import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.model.StampDesign;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignRepository;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignStationRepository;
import metro.ExoticStamp.modules.collection.domain.repository.StampDesignRepository;
import metro.ExoticStamp.modules.metro.application.port.LineReadPort;
import metro.ExoticStamp.modules.metro.application.port.StationReadPort;
import metro.ExoticStamp.modules.metro.application.view.MetroStationView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CollectionAdminCommandServiceTest {

    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private CampaignStationRepository campaignStationRepository;
    @Mock
    private StampDesignRepository stampDesignRepository;
    @Mock
    private LineReadPort lineReadPort;
    @Mock
    private StationReadPort stationReadPort;

    private CollectionAdminCommandService createService() {
        return new CollectionAdminCommandService(
                campaignRepository,
                campaignStationRepository,
                stampDesignRepository,
                lineReadPort,
                stationReadPort,
                new CollectionAdminMapper(),
                Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC)
        );
    }

    @Test
    void createStampDesign_withInactiveStation_throwsInvalidStation() {
        CollectionAdminCommandService service = createService();
        UUID stationId = UUID.randomUUID();

        when(stationReadPort.getStationViewById(stationId))
                .thenReturn(MetroStationView.builder()
                        .id(stationId)
                        .lineId(UUID.randomUUID())
                        .name("S1")
                        .sequence(1)
                        .active(false)
                        .build());

        AdminCreateStampDesignCommand cmd = new AdminCreateStampDesignCommand(
                stationId,
                null,
                "Design 1",
                "https://cdn/design.png",
                null,
                null,
                false,
                true
        );

        assertThrows(InvalidStationException.class, () -> service.createStampDesign(cmd));
    }

    @Test
    void assignStationToCampaign_withInactiveStation_throwsInvalidStation() {
        CollectionAdminCommandService service = createService();
        UUID campaignId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        UUID stationId = UUID.randomUUID();

        Campaign campaign = Campaign.builder()
                .id(campaignId)
                .lineId(lineId)
                .code("C1")
                .name("Campaign")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .isActive(true)
                .isDefault(false)
                .build();

        when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(campaign));
        when(stationReadPort.getStationViewById(stationId))
                .thenReturn(MetroStationView.builder()
                        .id(stationId)
                        .lineId(lineId)
                        .name("S1")
                        .sequence(1)
                        .active(false)
                        .build());

        assertThrows(InvalidStationException.class, () -> service.assignStationToCampaign(campaignId, stationId));
    }

    @Test
    void createStampDesign_withStationAndCampaignOnSameLine_succeeds() {
        CollectionAdminCommandService service = createService();
        UUID campaignId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        UUID stationId = UUID.randomUUID();

        Campaign campaign = Campaign.builder()
                .id(campaignId)
                .lineId(lineId)
                .code("C1")
                .name("Campaign")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .isActive(true)
                .isDefault(false)
                .build();
        when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(campaign));
        when(stationReadPort.getStationViewById(stationId))
                .thenReturn(MetroStationView.builder()
                        .id(stationId)
                        .lineId(lineId)
                        .name("S1")
                        .sequence(1)
                        .active(true)
                        .build());
        when(stampDesignRepository.save(any(StampDesign.class))).thenAnswer(inv -> inv.getArgument(0));

        AdminCreateStampDesignCommand cmd = new AdminCreateStampDesignCommand(
                stationId,
                campaignId,
                "Design 1",
                "https://cdn/design.png",
                null,
                null,
                false,
                true
        );

        service.createStampDesign(cmd);
        verify(stampDesignRepository).save(any(StampDesign.class));
    }
}

