package metro.ExoticStamp.modules.metro.application.mapper;

import metro.ExoticStamp.modules.metro.domain.model.Line;
import metro.ExoticStamp.modules.metro.domain.model.Station;
import metro.ExoticStamp.modules.metro.presentation.dto.response.LineDetailResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.LineResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationDetailResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MetroAppMapper {

    public LineResponse toLineResponse(Line line) {
        if (line == null) {
            return null;
        }

        return LineResponse.builder()
                .id(line.getId())
                .code(line.getCode())
                .name(line.getName())
                .color(line.getColor())
                .totalStations(line.getTotalStations())
                .isActive(Boolean.TRUE.equals(line.getIsActive()))
                .build();
    }

    public LineDetailResponse toLineDetailResponse(Line line, List<StationResponse> stationSummaries) {
        if (line == null) {
            return null;
        }
        return LineDetailResponse.builder()
                .id(line.getId())
                .code(line.getCode())
                .name(line.getName())
                .color(line.getColor())
                .totalStations(line.getTotalStations())
                .isActive(Boolean.TRUE.equals(line.getIsActive()))
                .stations(stationSummaries)
                .build();
    }

    public StationResponse toStationSummary(Station station) {
        if (station == null) {
            return null;
        }
        return StationResponse.builder()
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

    public StationDetailResponse toStationDetail(Station station, boolean includeSensitive) {
        if (station == null) {
            return null;
        }
        StationDetailResponse.StationDetailResponseBuilder b = StationDetailResponse.builder()
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
            b.nfcTagId(station.getNfcTagId())
                    .qrCodeToken(station.getQrCodeToken());
        }
        return b.build();
    }
}
