package metro.ExoticStamp.modules.metro.infrastructure.persistence;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.metro.domain.model.Station;
import metro.ExoticStamp.modules.metro.domain.repository.StationRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StationRepositoryAdapter implements StationRepository {

    private final JpaStationRepository jpaStationRepository;

    @Override
    public Optional<Station> findById(UUID id) {
        return jpaStationRepository.findById(id);
    }

    @Override
    public Station save(Station station) {
        return jpaStationRepository.save(station);
    }

    @Override
    public List<Station> findAllByLineId(UUID lineId) {
        return jpaStationRepository.findAllByLineIdOrderBySequenceAsc(lineId);
    }

    @Override
    public List<Station> findAllByLineIdAndIsActive(UUID lineId, boolean active) {
        return jpaStationRepository.findAllByLineIdAndIsActiveOrderBySequenceAsc(lineId, active);
    }

    @Override
    public List<Station> findAllStationsOrdered() {
        return jpaStationRepository.findAllByOrderByLineIdAscSequenceAsc();
    }

    @Override
    public List<Station> findAllActiveStations() {
        return jpaStationRepository.findAllByIsActiveOrderByLineIdAscSequenceAsc(true);
    }

    @Override
    public Optional<Station> findByNfcTagId(String nfcTagId) {
        return jpaStationRepository.findByNfcTagId(nfcTagId);
    }

    @Override
    public Optional<Station> findByQrCodeToken(String qrCodeToken) {
        return jpaStationRepository.findByQrCodeToken(qrCodeToken);
    }

    @Override
    public boolean existsByLineIdAndCode(UUID lineId, String code) {
        return jpaStationRepository.existsByLineIdAndCode(lineId, code);
    }

    @Override
    public boolean existsByLineIdAndCodeAndIdNot(UUID lineId, String code, UUID id) {
        return jpaStationRepository.existsByLineIdAndCodeAndIdNot(lineId, code, id);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaStationRepository.existsByCode(code);
    }

    @Override
    public boolean existsByCodeAndIdNot(String code, UUID id) {
        return jpaStationRepository.existsByCodeAndIdNot(code, id);
    }

    @Override
    public boolean existsByNfcTagId(String nfcTagId) {
        return jpaStationRepository.existsByNfcTagId(nfcTagId);
    }

    @Override
    public boolean existsByQrCodeToken(String qrToken) {
        return jpaStationRepository.existsByQrCodeToken(qrToken);
    }

    @Override
    public boolean existsByNfcTagIdAndIdNot(String nfcTagId, UUID id) {
        return jpaStationRepository.existsByNfcTagIdAndIdNot(nfcTagId, id);
    }

    @Override
    public boolean existsByQrCodeTokenAndIdNot(String qrToken, UUID id) {
        return jpaStationRepository.existsByQrCodeTokenAndIdNot(qrToken, id);
    }

    @Override
    public boolean existsByLineIdAndSequence(UUID lineId, Integer sequence) {
        return jpaStationRepository.existsByLineIdAndSequence(lineId, sequence);
    }

    @Override
    public boolean existsByLineIdAndSequenceAndIdNot(UUID lineId, Integer sequence, UUID id) {
        return jpaStationRepository.existsByLineIdAndSequenceAndIdNot(lineId, sequence, id);
    }

    @Override
    public List<Object[]> findTop20StationStatsRaw() {
        return jpaStationRepository.findTop20StationStatsRaw();
    }
}



