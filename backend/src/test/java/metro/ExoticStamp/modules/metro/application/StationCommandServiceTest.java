package metro.ExoticStamp.modules.metro.application;

import metro.ExoticStamp.common.exceptions.storage.InvalidImageTypeException;
import metro.ExoticStamp.infra.storage.FileValidator;
import metro.ExoticStamp.infra.storage.StorageProperties;
import metro.ExoticStamp.infra.storage.StorageService;
import metro.ExoticStamp.modules.metro.application.command.CreateStationCommand;
import metro.ExoticStamp.modules.metro.application.command.RotateStationQrTokenCommand;
import metro.ExoticStamp.modules.metro.application.command.UpdateStationCommand;
import metro.ExoticStamp.modules.metro.application.mapper.MetroAppMapper;
import metro.ExoticStamp.modules.metro.application.port.StationCachePort;
import metro.ExoticStamp.modules.metro.application.view.StationDetailView;
import metro.ExoticStamp.modules.metro.domain.event.StationQrRotatedEvent;
import metro.ExoticStamp.modules.metro.domain.exception.DuplicateNfcTagException;
import metro.ExoticStamp.modules.metro.domain.exception.LineNotFoundException;
import metro.ExoticStamp.modules.metro.domain.exception.StationInactiveException;
import metro.ExoticStamp.modules.metro.domain.exception.StationNotFoundException;
import metro.ExoticStamp.modules.metro.domain.model.Line;
import metro.ExoticStamp.modules.metro.domain.model.Station;
import metro.ExoticStamp.modules.metro.domain.repository.LineRepository;
import metro.ExoticStamp.modules.metro.domain.repository.StationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StationCommandServiceTest {
    private static final UUID LINE_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID STATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000501");


    @Mock
    private LineRepository lineRepository;

    @Mock
    private StationRepository stationRepository;

    @Mock
    private StationCachePort stationCachePort;

    @Mock
    private MetroAppMapper mapper;

    @Mock
    private StorageService storageService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private StationCommandService stationCommandService;

    @BeforeEach
    void setUp() {
        StorageProperties props = new StorageProperties();
        props.getFile().setMaxSizeMb(5);
        props.getFile().setAllowedTypes(List.of("image/jpeg", "image/png"));
        FileValidator fileValidator = new FileValidator(props);
        stationCommandService = new StationCommandService(
                lineRepository,
                stationRepository,
                stationCachePort,
                mapper,
                storageService,
                fileValidator,
                eventPublisher
        );
    }

    @Test
    void createStation_success() {
        when(lineRepository.findById(LINE_ID)).thenReturn(Optional.of(sampleLine()));
        when(stationRepository.existsByLineIdAndSequence(LINE_ID, 1)).thenReturn(false);
        when(stationRepository.existsByCode("S1")).thenReturn(false);
        when(stationRepository.existsByNfcTagId("NFC1")).thenReturn(false);
        when(stationRepository.existsByQrCodeToken("QR1")).thenReturn(false);
        when(stationRepository.save(any(Station.class))).thenAnswer(inv -> {
            Station s = inv.getArgument(0);
            s.setId(STATION_ID);
            return s;
        });
        when(lineRepository.save(any(Line.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toStationDetailView(any(Station.class), eq(true))).thenReturn(StationDetailView.builder().id(STATION_ID).build());

        CreateStationCommand req = CreateStationCommand.builder()
                .lineId(LINE_ID)
                .code("S1")
                .name("Station")
                .sequence(1)
                .isActive(true)
                .nfcTagId("NFC1")
                .qrCodeToken("QR1")
                .latitude(BigDecimal.ONE)
                .longitude(BigDecimal.ONE)
                .build();

        stationCommandService.createStation(req);

        verify(stationRepository).save(any(Station.class));
        verify(lineRepository).save(any(Line.class));
    }

    @Test
    void createStation_lineNotFound_throws() {
        when(lineRepository.findById(LINE_ID)).thenReturn(Optional.empty());
        CreateStationCommand req = CreateStationCommand.builder()
                .lineId(LINE_ID)
                .code("S1").name("N").sequence(1).isActive(true).build();
        assertThrows(LineNotFoundException.class, () -> stationCommandService.createStation(req));
    }

    @Test
    void createStation_duplicateNfc_throws() {
        when(lineRepository.findById(LINE_ID)).thenReturn(Optional.of(sampleLine()));
        when(stationRepository.existsByLineIdAndSequence(LINE_ID, 1)).thenReturn(false);
        when(stationRepository.existsByCode("S1")).thenReturn(false);
        when(stationRepository.existsByNfcTagId("NFC_DUP")).thenReturn(true);

        CreateStationCommand req = CreateStationCommand.builder()
                .lineId(LINE_ID)
                .code("S1").name("Station").sequence(1).isActive(true).nfcTagId("NFC_DUP").build();

        assertThrows(DuplicateNfcTagException.class, () -> stationCommandService.createStation(req));
    }

    @Test
    void updateStation_notFound_throws() {
        when(stationRepository.findById(STATION_ID)).thenReturn(Optional.empty());
        assertThrows(StationNotFoundException.class,
                () -> stationCommandService.updateStation(UpdateStationCommand.builder().stationId(STATION_ID).build()));
    }

    @Test
    void deactivate_evictsCachesAfterUpdate() {
        Station st = sampleStation();
        when(stationRepository.findById(STATION_ID)).thenReturn(Optional.of(st));
        when(stationRepository.save(any(Station.class))).thenAnswer(inv -> inv.getArgument(0));
        when(lineRepository.findById(LINE_ID)).thenReturn(Optional.of(sampleLine()));
        when(lineRepository.save(any(Line.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toStationDetailView(any(), eq(true))).thenReturn(StationDetailView.builder().id(STATION_ID).build());

        stationCommandService.deactivateStation(STATION_ID);

        verify(stationCachePort).evictDetailByStationId(STATION_ID);
    }

    @Test
    void rotateQr_publishesEventAndEvictsOldToken() {
        Station st = sampleStation();
        st.setQrCodeToken("OLD_QR");
        when(stationRepository.findById(STATION_ID)).thenReturn(Optional.of(st));
        when(stationRepository.existsByQrCodeTokenAndIdNot("NEW_QR", STATION_ID)).thenReturn(false);
        when(stationRepository.save(any(Station.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toStationDetailView(any(), eq(true))).thenReturn(StationDetailView.builder().id(STATION_ID).build());

        stationCommandService.rotateQrToken(new RotateStationQrTokenCommand(STATION_ID, "NEW_QR"));

        verify(stationCachePort).evictByQrToken("OLD_QR");
        verify(stationCachePort).evictDetailByStationId(STATION_ID);
        ArgumentCaptor<StationQrRotatedEvent> cap = ArgumentCaptor.forClass(StationQrRotatedEvent.class);
        verify(eventPublisher).publishEvent(cap.capture());
        org.junit.jupiter.api.Assertions.assertEquals(STATION_ID, cap.getValue().stationId());
        org.junit.jupiter.api.Assertions.assertEquals("OLD_QR", cap.getValue().oldQrToken());
        org.junit.jupiter.api.Assertions.assertEquals("NEW_QR", cap.getValue().newQrToken());
    }

    @Test
    void uploadImage_invalidType_throws() {
        MockMultipartFile file = new MockMultipartFile("file", "a.gif", "image/gif", new byte[10]);
        assertThrows(InvalidImageTypeException.class, () -> stationCommandService.uploadStationImage(STATION_ID, file));
    }

    @Test
    void uploadImage_fileTooLarge_throws() {
        byte[] huge = new byte[6 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile("file", "a.jpg", "image/jpeg", huge);
        assertThrows(metro.ExoticStamp.common.exceptions.storage.FileTooLargeException.class,
                () -> stationCommandService.uploadStationImage(STATION_ID, file));
    }

    @Test
    void uploadImage_replacesOldImage_deletesOldFile() {
        Station st = sampleStation();
        st.setImageUrl("http://localhost:8080/uploads/old.jpg");
        when(stationRepository.findById(STATION_ID)).thenReturn(Optional.of(st));
        when(stationRepository.save(any(Station.class))).thenAnswer(inv -> inv.getArgument(0));
        when(storageService.upload(any(), eq("metro/stations/" + STATION_ID))).thenReturn("http://localhost:8080/uploads/new.jpg");

        MockMultipartFile file = new MockMultipartFile("file", "a.jpg", "image/jpeg", new byte[20]);
        stationCommandService.uploadStationImage(STATION_ID, file);

        verify(storageService).delete(eq("http://localhost:8080/uploads/old.jpg"));
        verify(storageService).upload(any(), eq("metro/stations/" + STATION_ID));
    }

    @Test
    void softDelete_evictsAllRelatedCaches() {
        Station st = sampleStation();
        st.setNfcTagId("NFC");
        st.setQrCodeToken("QR");
        when(stationRepository.findById(STATION_ID)).thenReturn(Optional.of(st));
        when(lineRepository.findById(LINE_ID)).thenReturn(Optional.of(sampleLine()));
        when(lineRepository.save(any(Line.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stationRepository.save(any(Station.class))).thenAnswer(inv -> inv.getArgument(0));

        stationCommandService.softDeleteStation(STATION_ID);

        verify(stationCachePort).evictDetailByStationId(STATION_ID);
        verify(stationCachePort).evictByNfcTagId("NFC");
        verify(stationCachePort).evictByQrToken("QR");
    }

    @Test
    void incrementCollectorCount_inactiveStation_throws() {
        Station st = sampleStation();
        st.setIsActive(false);
        when(stationRepository.findById(STATION_ID)).thenReturn(Optional.of(st));

        assertThrows(StationInactiveException.class, () -> stationCommandService.incrementCollectorCount(STATION_ID));
    }

    @Test
    void incrementCollectorCount_overflow_throws() {
        Station st = sampleStation();
        st.setCollectorCount(Integer.MAX_VALUE);
        when(stationRepository.findById(STATION_ID)).thenReturn(Optional.of(st));

        assertThrows(IllegalArgumentException.class, () -> stationCommandService.incrementCollectorCount(STATION_ID));
    }

    private static Line sampleLine() {
        return Line.builder()
                .id(LINE_ID)
                .code("L1")
                .name("Line")
                .totalStations(0)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private static Station sampleStation() {
        return Station.builder()
                .id(STATION_ID)
                .lineId(LINE_ID)
                .code("S1")
                .name("S")
                .sequence(1)
                .collectorCount(0)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
