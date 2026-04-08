package metro.ExoticStamp.modules.metro.infrastructure.persistence;

import metro.ExoticStamp.modules.metro.domain.model.Station;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StationRepositoryAdapterTest {

    @Mock
    private JpaStationRepository jpaStationRepository;

    @InjectMocks
    private StationRepositoryAdapter adapter;

    @Test
    void findByNfcTagId_delegatesAndReturnsSameEntity() {
        Station station = Station.builder().id(1).nfcTagId("NFC_1").isActive(true).build();
        when(jpaStationRepository.findByNfcTagId("NFC_1")).thenReturn(Optional.of(station));

        Optional<Station> res = adapter.findByNfcTagId("NFC_1");

        assertEquals(true, res.isPresent());
        assertSame(station, res.get());
    }

    @Test
    void findAllByLineIdAndIsActive_ordersBySequence() {
        Station s1 = Station.builder().id(1).sequence(1).build();
        List<Station> expected = List.of(s1);
        when(jpaStationRepository.findAllByLineIdAndIsActiveOrderBySequenceAsc(10, true)).thenReturn(expected);

        List<Station> res = adapter.findAllByLineIdAndIsActive(10, true);

        assertSame(expected, res);
    }
}

