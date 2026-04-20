package metro.ExoticStamp.modules.metro.infrastructure.persistence;

import metro.ExoticStamp.modules.metro.domain.model.Station;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StationRepositoryAdapterTest {
    private static final UUID LINE_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID STATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Mock
    private JpaStationRepository jpaStationRepository;

    @InjectMocks
    private StationRepositoryAdapter adapter;

    @Test
    void findByNfcTagId_delegatesAndReturnsSameEntity() {
        Station station = Station.builder().id(STATION_ID).nfcTagId("NFC_1").isActive(true).build();
        when(jpaStationRepository.findByNfcTagId("NFC_1")).thenReturn(Optional.of(station));

        Optional<Station> res = adapter.findByNfcTagId("NFC_1");

        assertEquals(true, res.isPresent());
        assertSame(station, res.get());
    }

    @Test
    void findAllByLineIdAndIsActive_ordersBySequence() {
        Station s1 = Station.builder().id(STATION_ID).sequence(1).build();
        List<Station> expected = List.of(s1);
        when(jpaStationRepository.findAllByLineIdAndIsActiveOrderBySequenceAsc(LINE_ID, true)).thenReturn(expected);

        List<Station> res = adapter.findAllByLineIdAndIsActive(LINE_ID, true);

        assertSame(expected, res);
    }
}

