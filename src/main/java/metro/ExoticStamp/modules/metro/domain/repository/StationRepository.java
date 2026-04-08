package metro.ExoticStamp.modules.metro.domain.repository;

import java.util.UUID;

import metro.ExoticStamp.modules.metro.domain.model.Station;

import java.util.List;
import java.util.Optional;

public interface StationRepository {

    Optional<Station> findById(UUID id);

    Station save(Station station);

    List<Station> findAllByLineId(UUID lineId);

    List<Station> findAllByLineIdAndIsActive(UUID lineId, boolean active);

    List<Station> findAllStationsOrdered();

    List<Station> findAllActiveStations();

    Optional<Station> findByNfcTagId(String nfcTagId);

    Optional<Station> findByQrCodeToken(String qrCodeToken);

    boolean existsByLineIdAndCode(UUID lineId, String code);

    boolean existsByLineIdAndCodeAndIdNot(UUID lineId, String code, UUID id);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, UUID id);

    boolean existsByNfcTagId(String nfcTagId);

    boolean existsByQrCodeToken(String qrToken);

    boolean existsByNfcTagIdAndIdNot(String nfcTagId, UUID id);

    boolean existsByQrCodeTokenAndIdNot(String qrToken, UUID id);

    boolean existsByLineIdAndSequence(UUID lineId, Integer sequence);

    boolean existsByLineIdAndSequenceAndIdNot(UUID lineId, Integer sequence, UUID id);

    List<Object[]> findTop20StationStatsRaw();
}



