package metro.ExoticStamp.modules.metro.application;

import metro.ExoticStamp.modules.metro.application.mapper.MetroAppMapper;
import metro.ExoticStamp.modules.metro.application.port.StationCachePort;
import metro.ExoticStamp.modules.metro.domain.exception.StationInactiveException;
import metro.ExoticStamp.modules.metro.domain.exception.StationNotFoundException;
import metro.ExoticStamp.modules.metro.domain.model.Station;
import metro.ExoticStamp.modules.metro.domain.repository.LineRepository;
import metro.ExoticStamp.modules.metro.domain.repository.StationRepository;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationDetailResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StationQueryServiceTest {

    @Mock
    private LineRepository lineRepository;

    @Mock
    private StationRepository stationRepository;

    @Mock
    private StationCachePort stationCachePort;

    private final MetroAppMapper mapper = new MetroAppMapper();

    private StationQueryService service;

    @BeforeEach
    void setUp() {
        service = new StationQueryService(lineRepository, stationRepository, stationCachePort, mapper);
    }

    @Test
    void resolveStationByNfc_success_dbThenCaches() {
        Station station = Station.builder()
                .id(10)
                .lineId(1)
                .code("S10")
                .name("Central")
                .sequence(1)
                .collectorCount(0)
                .isActive(true)
                .nfcTagId("NFC_10")
                .build();

        when(stationCachePort.getByNfcTagId("NFC_10")).thenReturn(Optional.empty());
        when(stationRepository.findByNfcTagId("NFC_10")).thenReturn(Optional.of(station));

        StationDetailResponse res = service.resolveStationByNfc("NFC_10");

        assertEquals(10, res.getId());
        assertEquals("Central", res.getName());
        verify(stationCachePort).putByNfcTagId(org.mockito.ArgumentMatchers.eq("NFC_10"), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void resolveStationByNfc_notFound_throws() {
        when(stationCachePort.getByNfcTagId("NFC_X")).thenReturn(Optional.empty());
        when(stationRepository.findByNfcTagId("NFC_X")).thenReturn(Optional.empty());

        assertThrows(StationNotFoundException.class, () -> service.resolveStationByNfc("NFC_X"));
        verify(stationCachePort, never()).putByNfcTagId(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void resolveStationByQr_success_dbThenCaches() {
        Station station = Station.builder()
                .id(20)
                .lineId(1)
                .code("S20")
                .name("Airport")
                .sequence(2)
                .collectorCount(0)
                .isActive(true)
                .qrCodeToken("QR_20")
                .build();

        when(stationCachePort.getByQrToken("QR_20")).thenReturn(Optional.empty());
        when(stationRepository.findByQrCodeToken("QR_20")).thenReturn(Optional.of(station));

        StationDetailResponse res = service.resolveStationByQr("QR_20");

        assertEquals(20, res.getId());
        assertEquals("Airport", res.getName());
        verify(stationCachePort).putByQrToken(org.mockito.ArgumentMatchers.eq("QR_20"), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void resolveStationByNfc_inactiveRejected() {
        Station station = Station.builder()
                .id(30)
                .lineId(1)
                .code("S30")
                .name("Closed")
                .sequence(3)
                .collectorCount(0)
                .isActive(false)
                .nfcTagId("NFC_30")
                .build();

        when(stationCachePort.getByNfcTagId("NFC_30")).thenReturn(Optional.empty());
        when(stationRepository.findByNfcTagId("NFC_30")).thenReturn(Optional.of(station));

        assertThrows(StationInactiveException.class, () -> service.resolveStationByNfc("NFC_30"));
    }
}
