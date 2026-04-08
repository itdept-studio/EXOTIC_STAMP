package metro.ExoticStamp.modules.metro.infrastructure.persistence;

import metro.ExoticStamp.modules.metro.domain.model.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface JpaStationRepository extends JpaRepository<Station, Integer> {

    List<Station> findAllByLineIdOrderBySequenceAsc(Integer lineId);

    List<Station> findAllByLineIdAndIsActiveOrderBySequenceAsc(Integer lineId, boolean isActive);

    List<Station> findAllByOrderByLineIdAscSequenceAsc();

    List<Station> findAllByIsActiveOrderByLineIdAscSequenceAsc(boolean active);

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

    @Query(value = """
            SELECT s.id, s.name, l.name, s.collector_count
            FROM stations s
            JOIN lines l ON l.id = s.line_id
            ORDER BY s.collector_count DESC
            LIMIT 20
            """, nativeQuery = true)
    List<Object[]> findTop20StationStatsRaw();
}
