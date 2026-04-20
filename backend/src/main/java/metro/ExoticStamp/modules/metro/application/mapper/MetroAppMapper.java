package metro.ExoticStamp.modules.metro.application.mapper;

import metro.ExoticStamp.modules.metro.application.view.LineDetailView;
import metro.ExoticStamp.modules.metro.application.view.LineView;
import metro.ExoticStamp.modules.metro.application.view.StationDetailView;
import metro.ExoticStamp.modules.metro.application.view.StationView;
import metro.ExoticStamp.modules.metro.domain.model.Line;
import metro.ExoticStamp.modules.metro.domain.model.Station;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MetroAppMapper {

    public LineView toLineView(Line line) {
        if (line == null) {
            return null;
        }
        return LineView.builder()
                .id(line.getId())
                .code(line.getCode())
                .name(line.getName())
                .color(line.getColor())
                .totalStations(line.getTotalStations())
                .isActive(Boolean.TRUE.equals(line.getIsActive()))
                .build();
    }

    public LineDetailView toLineDetailView(Line line, List<StationView> stationViews) {
        if (line == null) {
            return null;
        }
        return LineDetailView.builder()
                .id(line.getId())
                .code(line.getCode())
                .name(line.getName())
                .color(line.getColor())
                .totalStations(line.getTotalStations())
                .isActive(Boolean.TRUE.equals(line.getIsActive()))
                .stations(stationViews)
                .build();
    }

    public StationView toStationView(Station station) {
        if (station == null) {
            return null;
        }
        return StationView.builder()
                .id(station.getId())
                .lineId(station.getLineId())
                .code(station.getCode())
                .name(station.getName())
                .sequence(station.getSequence())
                .description(station.getDescription())
                .historicalInfo(station.getHistoricalInfo())
                .imageUrl(station.getImageUrl())
                .latitude(station.getLatitude())
                .longitude(station.getLongitude())
                .collectorCount(station.getCollectorCount())
                .isActive(Boolean.TRUE.equals(station.getIsActive()))
                .build();
    }

    public StationDetailView toStationDetailView(Station station, boolean includeSensitive) {
        if (station == null) {
            return null;
        }
        StationDetailView.StationDetailViewBuilder builder = StationDetailView.builder()
                .id(station.getId())
                .lineId(station.getLineId())
                .code(station.getCode())
                .name(station.getName())
                .sequence(station.getSequence())
                .description(station.getDescription())
                .historicalInfo(station.getHistoricalInfo())
                .imageUrl(station.getImageUrl())
                .latitude(station.getLatitude())
                .longitude(station.getLongitude())
                .collectorCount(station.getCollectorCount())
                .isActive(Boolean.TRUE.equals(station.getIsActive()));
        if (includeSensitive) {
            builder.nfcTagId(station.getNfcTagId())
                    .qrCodeToken(station.getQrCodeToken());
        }
        return builder.build();
    }
}
