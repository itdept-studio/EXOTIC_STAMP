package metro.ExoticStamp.modules.metro.presentation.mapper;

import metro.ExoticStamp.modules.metro.application.command.CreateLineCommand;
import metro.ExoticStamp.modules.metro.application.command.CreateStationCommand;
import metro.ExoticStamp.modules.metro.application.command.RotateStationQrTokenCommand;
import metro.ExoticStamp.modules.metro.application.command.ToggleLineStatusCommand;
import metro.ExoticStamp.modules.metro.application.command.UpdateLineCommand;
import metro.ExoticStamp.modules.metro.application.command.UpdateStationCommand;
import metro.ExoticStamp.modules.metro.application.view.LineDetailView;
import metro.ExoticStamp.modules.metro.application.view.LineView;
import metro.ExoticStamp.modules.metro.application.view.StationDetailView;
import metro.ExoticStamp.modules.metro.application.view.StationImageUploadView;
import metro.ExoticStamp.modules.metro.application.view.StationStatsView;
import metro.ExoticStamp.modules.metro.application.view.StationView;
import metro.ExoticStamp.modules.metro.presentation.dto.request.CreateLineRequest;
import metro.ExoticStamp.modules.metro.presentation.dto.request.CreateStationRequest;
import metro.ExoticStamp.modules.metro.presentation.dto.request.RotateQrTokenRequest;
import metro.ExoticStamp.modules.metro.presentation.dto.request.ToggleStatusRequest;
import metro.ExoticStamp.modules.metro.presentation.dto.request.UpdateLineRequest;
import metro.ExoticStamp.modules.metro.presentation.dto.request.UpdateStationRequest;
import metro.ExoticStamp.modules.metro.presentation.dto.response.LineDetailResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.LineResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationDetailResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationImageUploadResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationStatsResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class MetroPresentationMapper {

    public CreateLineCommand toCreateLineCommand(CreateLineRequest request) {
        return CreateLineCommand.builder()
                .code(request.getCode())
                .name(request.getName())
                .color(request.getColor())
                .build();
    }

    public UpdateLineCommand toUpdateLineCommand(UUID lineId, UpdateLineRequest request) {
        return UpdateLineCommand.builder()
                .lineId(lineId)
                .code(request.getCode())
                .name(request.getName())
                .color(request.getColor())
                .isActive(request.getIsActive())
                .build();
    }

    public ToggleLineStatusCommand toToggleLineStatusCommand(UUID lineId, ToggleStatusRequest request) {
        return ToggleLineStatusCommand.builder()
                .lineId(lineId)
                .isActive(request.getIsActive())
                .build();
    }

    public CreateStationCommand toCreateStationCommand(CreateStationRequest request) {
        return CreateStationCommand.builder()
                .code(request.getCode())
                .name(request.getName())
                .lineId(request.getLineId())
                .sequence(request.getSequence())
                .description(request.getDescription())
                .historicalInfo(request.getHistoricalInfo())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .nfcTagId(request.getNfcTagId())
                .qrCodeToken(request.getQrCodeToken())
                .isActive(request.getIsActive())
                .build();
    }

    public UpdateStationCommand toUpdateStationCommand(UUID stationId, UpdateStationRequest request) {
        return UpdateStationCommand.builder()
                .stationId(stationId)
                .code(request.getCode())
                .name(request.getName())
                .sequence(request.getSequence())
                .description(request.getDescription())
                .historicalInfo(request.getHistoricalInfo())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .nfcTagId(request.getNfcTagId())
                .qrCodeToken(request.getQrCodeToken())
                .isActive(request.getIsActive())
                .build();
    }

    public RotateStationQrTokenCommand toRotateQrTokenCommand(UUID stationId, RotateQrTokenRequest request) {
        return RotateStationQrTokenCommand.builder()
                .stationId(stationId)
                .qrCodeToken(request.getQrCodeToken())
                .build();
    }

    public LineResponse toResponse(LineView view) {
        return LineResponse.builder()
                .id(view.getId())
                .code(view.getCode())
                .name(view.getName())
                .color(view.getColor())
                .totalStations(view.getTotalStations())
                .isActive(view.isActive())
                .build();
    }

    public List<LineResponse> toLineResponses(List<LineView> views) {
        return views.stream().map(this::toResponse).toList();
    }

    public LineDetailResponse toResponse(LineDetailView view) {
        return LineDetailResponse.builder()
                .id(view.getId())
                .code(view.getCode())
                .name(view.getName())
                .color(view.getColor())
                .totalStations(view.getTotalStations())
                .isActive(view.isActive())
                .stations(toStationResponses(view.getStations()))
                .build();
    }

    public StationResponse toResponse(StationView view) {
        return StationResponse.builder()
                .id(view.getId())
                .lineId(view.getLineId())
                .code(view.getCode())
                .name(view.getName())
                .sequence(view.getSequence())
                .description(view.getDescription())
                .historicalInfo(view.getHistoricalInfo())
                .imageUrl(view.getImageUrl())
                .latitude(view.getLatitude())
                .longitude(view.getLongitude())
                .collectorCount(view.getCollectorCount())
                .isActive(view.isActive())
                .build();
    }

    public List<StationResponse> toStationResponses(List<StationView> views) {
        return views.stream().map(this::toResponse).toList();
    }

    public StationDetailResponse toResponse(StationDetailView view) {
        return StationDetailResponse.builder()
                .id(view.getId())
                .lineId(view.getLineId())
                .code(view.getCode())
                .name(view.getName())
                .sequence(view.getSequence())
                .description(view.getDescription())
                .historicalInfo(view.getHistoricalInfo())
                .imageUrl(view.getImageUrl())
                .latitude(view.getLatitude())
                .longitude(view.getLongitude())
                .nfcTagId(view.getNfcTagId())
                .qrCodeToken(view.getQrCodeToken())
                .collectorCount(view.getCollectorCount())
                .isActive(view.isActive())
                .build();
    }

    public StationStatsResponse toResponse(StationStatsView view) {
        return StationStatsResponse.builder()
                .stationId(view.getStationId())
                .stationName(view.getStationName())
                .lineName(view.getLineName())
                .collectorCount(view.getCollectorCount())
                .build();
    }

    public List<StationStatsResponse> toStationStatsResponses(List<StationStatsView> views) {
        return views.stream().map(this::toResponse).toList();
    }

    public StationImageUploadResponse toResponse(StationImageUploadView view) {
        return new StationImageUploadResponse(view.getImageUrl());
    }
}
