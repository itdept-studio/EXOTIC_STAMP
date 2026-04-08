package metro.ExoticStamp.modules.metro.infrastructure.persistence;

import java.util.UUID;

import metro.ExoticStamp.modules.metro.domain.model.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface JpaStationRepository extends JpaRepository<Station, UUID> {

    List<Station> findAllByLineIdOrderBySequenceAsc(UUID lineId);

    List<Station> findAllByLineIdAndIsActiveOrderBySequenceAsc(UUID lineId, boolean isActive);

    List<Station> findAllByOrderByLineIdAscSequenceAsc();

    List<Station> findAllByIsActiveOrderByLineIdAscSequenceAsc(boolean active);

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

    @Query(value = """
            SELECT s.id, s.name, l.name, s.collector_count
            FROM stations s
            JOIN lines l ON l.id = s.line_id
            ORDER BY s.collector_count DESC
            LIMIT 20
            """, nativeQuery = true)
    List<Object[]> findTop20StationStatsRaw();
}



