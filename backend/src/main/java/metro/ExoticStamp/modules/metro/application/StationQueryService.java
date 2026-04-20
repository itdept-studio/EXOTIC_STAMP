package metro.ExoticStamp.modules.metro.application;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.metro.application.mapper.MetroAppMapper;
import metro.ExoticStamp.modules.metro.application.port.StationCachePort;
import metro.ExoticStamp.modules.metro.application.port.StationReadPort;
import metro.ExoticStamp.modules.metro.application.view.MetroStationView;
import metro.ExoticStamp.modules.metro.application.view.StationDetailView;
import metro.ExoticStamp.modules.metro.application.view.StationStatsView;
import metro.ExoticStamp.modules.metro.application.view.StationView;
import metro.ExoticStamp.modules.metro.domain.exception.LineNotFoundException;
import metro.ExoticStamp.modules.metro.domain.exception.StationInactiveException;
import metro.ExoticStamp.modules.metro.domain.exception.StationNotFoundException;
import metro.ExoticStamp.modules.metro.domain.model.Station;
import metro.ExoticStamp.modules.metro.domain.repository.LineRepository;
import metro.ExoticStamp.modules.metro.domain.repository.StationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StationQueryService implements StationReadPort {

    private final LineRepository lineRepository;
    private final StationRepository stationRepository;
    private final StationCachePort stationCachePort;
    private final MetroAppMapper mapper;

    public List<StationView> listStations(UUID lineId, boolean activeOnly) {
        if (lineId != null) {
            lineRepository.findById(lineId).orElseThrow(() -> new LineNotFoundException(lineId));
            List<Station> stations = activeOnly
                    ? stationRepository.findAllByLineIdAndIsActive(lineId, true)
                    : stationRepository.findAllByLineId(lineId);
            return stations.stream().map(mapper::toStationView).toList();
        }
        List<Station> all = activeOnly
                ? stationRepository.findAllActiveStations()
                : stationRepository.findAllStationsOrdered();
        return all.stream().map(mapper::toStationView).toList();
    }

    public StationDetailView getStationDetailById(UUID stationId) {
        StationDetailView cached = stationCachePort.getByStationId(stationId).orElse(null);
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
        StationDetailView detail = mapper.toStationDetailView(station, false);
        stationCachePort.putByStationId(stationId, detail);
        return detail;
    }

    public StationDetailView resolveStationByNfc(String nfcTagId) {
        StationDetailView cached = stationCachePort.getByNfcTagId(nfcTagId).orElse(null);
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
        StationDetailView detail = mapper.toStationDetailView(station, false);
        stationCachePort.putByNfcTagId(nfcTagId, detail);
        return detail;
    }

    public List<StationStatsView> stationStats() {
        return stationRepository.findTop20StationStatsRaw().stream()
                .map(row -> StationStatsView.builder()
                        .stationId((UUID) row[0])
                        .stationName((String) row[1])
                        .lineName((String) row[2])
                        .collectorCount(((Number) row[3]).intValue())
                        .build())
                .toList();
    }

    public StationDetailView resolveStationByQr(String qrCodeToken) {
        StationDetailView cached = stationCachePort.getByQrToken(qrCodeToken).orElse(null);
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
        StationDetailView detail = mapper.toStationDetailView(station, false);
        stationCachePort.putByQrToken(qrCodeToken, detail);
        return detail;
    }

    @Override
    public MetroStationView resolveStationViewByNfc(String nfcTagId) {
        Station station = stationRepository.findByNfcTagId(nfcTagId)
                .orElseThrow(() -> new StationNotFoundException("nfcTagId", nfcTagId));
        if (!Boolean.TRUE.equals(station.getIsActive())) {
            throw new StationInactiveException(station.getId());
        }
        return toSharedStationView(station);
    }

    @Override
    public MetroStationView resolveStationViewByQr(String qrToken) {
        Station station = stationRepository.findByQrCodeToken(qrToken)
                .orElseThrow(() -> new StationNotFoundException("qrCodeToken", qrToken));
        if (!Boolean.TRUE.equals(station.getIsActive())) {
            throw new StationInactiveException(station.getId());
        }
        return toSharedStationView(station);
    }

    @Override
    public MetroStationView getStationViewById(UUID stationId) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new StationNotFoundException(stationId));
        return toSharedStationView(station);
    }

    @Override
    public List<MetroStationView> listActiveStationsByLineId(UUID lineId) {
        lineRepository.findById(lineId).orElseThrow(() -> new LineNotFoundException(lineId));
        return stationRepository.findAllByLineIdAndIsActive(lineId, true).stream().map(this::toSharedStationView).toList();
    }

    @Override
    public List<MetroStationView> listStationViewsByIds(Collection<UUID> stationIds) {
        if (stationIds == null || stationIds.isEmpty()) {
            return List.of();
        }
        return stationRepository.findAllByIdIn(stationIds.stream().distinct().toList()).stream()
                .map(this::toSharedStationView)
                .toList();
    }

    private MetroStationView toSharedStationView(Station station) {
        return MetroStationView.builder()
                .id(station.getId())
                .lineId(station.getLineId())
                .name(station.getName())
                .sequence(station.getSequence())
                .active(Boolean.TRUE.equals(station.getIsActive()))
                .latitude(station.getLatitude())
                .longitude(station.getLongitude())
                .build();
    }

    private static StationDetailView stripSensitiveForPublic(StationDetailView detail) {
        return StationDetailView.builder()
                .id(detail.getId())
                .lineId(detail.getLineId())
                .code(detail.getCode())
                .name(detail.getName())
                .sequence(detail.getSequence())
                .description(detail.getDescription())
                .historicalInfo(detail.getHistoricalInfo())
                .imageUrl(detail.getImageUrl())
                .latitude(detail.getLatitude())
                .longitude(detail.getLongitude())
                .nfcTagId(null)
                .qrCodeToken(null)
                .collectorCount(detail.getCollectorCount())
                .isActive(detail.isActive())
                .build();
    }
}
