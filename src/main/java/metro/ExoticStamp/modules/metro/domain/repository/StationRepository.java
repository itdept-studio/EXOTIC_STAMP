package metro.ExoticStamp.modules.metro.domain.repository;

import metro.ExoticStamp.modules.metro.domain.model.Station;

import java.util.List;
import java.util.Optional;

public interface StationRepository {

    Optional<Station> findById(Integer id);

    Station save(Station station);

    List<Station> findAllByLineId(Integer lineId);

    List<Station> findAllByLineIdAndIsActive(Integer lineId, boolean active);

    List<Station> findAllStationsOrdered();

    List<Station> findAllActiveStations();

    Optional<Station> findByNfcTagId(String nfcTagId);

    Optional<Station> findByQrCodeToken(String qrCodeToken);

    boolean existsByLineIdAndCode(Integer lineId, String code);

    boolean existsByLineIdAndCodeAndIdNot(Integer lineId, String code, Integer id);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Integer id);

    boolean existsByNfcTagId(String nfcTagId);

    boolean existsByQrCodeToken(String qrToken);

    boolean existsByNfcTagIdAndIdNot(String nfcTagId, Integer id);

    boolean existsByQrCodeTokenAndIdNot(String qrToken, Integer id);

    boolean existsByLineIdAndSequence(Integer lineId, Integer sequence);

    boolean existsByLineIdAndSequenceAndIdNot(Integer lineId, Integer sequence, Integer id);

    List<Object[]> findTop20StationStatsRaw();
}
