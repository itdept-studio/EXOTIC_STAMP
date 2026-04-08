package metro.ExoticStamp.modules.metro.application;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.metro.application.mapper.MetroAppMapper;
import metro.ExoticStamp.modules.metro.application.port.StationCachePort;
import metro.ExoticStamp.modules.metro.domain.exception.LineNotFoundException;
import metro.ExoticStamp.modules.metro.domain.exception.StationInactiveException;
import metro.ExoticStamp.modules.metro.domain.exception.StationNotFoundException;
import metro.ExoticStamp.modules.metro.domain.model.Station;
import metro.ExoticStamp.modules.metro.domain.repository.LineRepository;
import metro.ExoticStamp.modules.metro.domain.repository.StationRepository;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationDetailResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationStatsResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StationQueryService {

    private final LineRepository lineRepository;
    private final StationRepository stationRepository;
    private final StationCachePort stationCachePort;
    private final MetroAppMapper mapper;

    public List<StationResponse> listStations(UUID lineId, boolean activeOnly) {
        if (lineId != null) {
            lineRepository.findById(lineId).orElseThrow(() -> new LineNotFoundException(lineId));
            List<Station> stations = activeOnly
                    ? stationRepository.findAllByLineIdAndIsActive(lineId, true)
                    : stationRepository.findAllByLineId(lineId);
            return stations.stream().map(mapper::toStationSummary).toList();
        }
        List<Station> all = activeOnly
                ? stationRepository.findAllActiveStations()
                : stationRepository.findAllStationsOrdered();
        return all.stream().map(mapper::toStationSummary).toList();
    }

    public StationDetailResponse getStationDetailById(UUID stationId) {
        StationDetailResponse cached = stationCachePort.getByStationId(stationId).orElse(null);
        if (cached != null) {
            if (!cached.isActive()) {
                throw new StationNotFoundException(stationId);
            }
            return stripSensitiveForPublic(cached);
        }

        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new StationNotFoundException(stationId));
        if (!Boolean.TRUE.equals(station.getIsActive())) {
            throw new StationNotFoundException(stationId);
        }
        StationDetailResponse detail = mapper.toStationDetail(station, false);
        stationCachePort.putByStationId(stationId, detail);
        return detail;
    }

    public StationDetailResponse resolveStationByNfc(String nfcTagId) {
        StationDetailResponse cached = stationCachePort.getByNfcTagId(nfcTagId).orElse(null);
        if (cached != null) {
            if (!cached.isActive()) {
                throw new StationInactiveException(cached.getId());
            }
            return stripSensitiveForPublic(cached);
        }

        Station station = stationRepository.findByNfcTagId(nfcTagId)
                .orElseThrow(() -> new StationNotFoundException("nfcTagId", nfcTagId));
        if (!Boolean.TRUE.equals(station.getIsActive())) {
            throw new StationInactiveException(station.getId());
        }
        StationDetailResponse detail = mapper.toStationDetail(station, false);
        stationCachePort.putByNfcTagId(nfcTagId, detail);
        return detail;
    }

    public List<StationStatsResponse> stationStats() {
        return stationRepository.findTop20StationStatsRaw().stream()
                .map(row -> StationStatsResponse.builder()
                        .stationId((UUID) row[0])
                        .stationName((String) row[1])
                        .lineName((String) row[2])
                        .collectorCount(((Number) row[3]).intValue())
                        .build())
                .toList();
    }

    public StationDetailResponse resolveStationByQr(String qrCodeToken) {
        StationDetailResponse cached = stationCachePort.getByQrToken(qrCodeToken).orElse(null);
        if (cached != null) {
            if (!cached.isActive()) {
                throw new StationInactiveException(cached.getId());
            }
            return stripSensitiveForPublic(cached);
        }

        Station station = stationRepository.findByQrCodeToken(qrCodeToken)
                .orElseThrow(() -> new StationNotFoundException("qrCodeToken", qrCodeToken));
        if (!Boolean.TRUE.equals(station.getIsActive())) {
            throw new StationInactiveException(station.getId());
        }
        StationDetailResponse detail = mapper.toStationDetail(station, false);
        stationCachePort.putByQrToken(qrCodeToken, detail);
        return detail;
    }

    /** Public responses never include NFC/QR even if cache was populated incorrectly. */
    private static StationDetailResponse stripSensitiveForPublic(StationDetailResponse d) {
        return StationDetailResponse.builder()
                .id(d.getId())
                .lineId(d.getLineId())
                .code(d.getCode())
                .name(d.getName())
                .sequence(d.getSequence())
                .description(d.getDescription())
                .historicalInfo(d.getHistoricalInfo())
                .imageUrl(d.getImageUrl())
                .latitude(d.getLatitude())
                .longitude(d.getLongitude())
                .nfcTagId(null)
                .qrCodeToken(null)
                .collectorCount(d.getCollectorCount())
                .isActive(d.isActive())
                .build();
    }
}





