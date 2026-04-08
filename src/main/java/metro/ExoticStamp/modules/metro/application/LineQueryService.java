package metro.ExoticStamp.modules.metro.application;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.metro.application.mapper.MetroAppMapper;
import metro.ExoticStamp.modules.metro.application.port.LineReadPort;
import metro.ExoticStamp.modules.metro.application.view.MetroLineView;
import metro.ExoticStamp.modules.metro.domain.exception.LineNotFoundException;
import metro.ExoticStamp.modules.metro.domain.model.Line;
import metro.ExoticStamp.modules.metro.domain.model.Station;
import metro.ExoticStamp.modules.metro.domain.repository.LineRepository;
import metro.ExoticStamp.modules.metro.domain.repository.StationRepository;
import metro.ExoticStamp.modules.metro.presentation.dto.response.LineDetailResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.LineResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LineQueryService implements LineReadPort {

    private final LineRepository lineRepository;
    private final StationRepository stationRepository;
    private final MetroAppMapper mapper;

    public List<LineResponse> getAllLines(boolean activeOnly) {
        List<Line> lines = activeOnly
                ? lineRepository.findAllByIsActive(true)
                : lineRepository.findAll();
        return lines.stream().map(mapper::toLineResponse).toList();
    }

    public LineDetailResponse getLineDetail(UUID lineId, boolean stationsActiveOnly) {
        Line line = lineRepository.findById(lineId).orElseThrow(() -> new LineNotFoundException(lineId));
        List<Station> stations = stationsActiveOnly
                ? stationRepository.findAllByLineIdAndIsActive(lineId, true)
                : stationRepository.findAllByLineId(lineId);
        List<StationResponse> summaries = stations.stream().map(mapper::toStationSummary).toList();
        return mapper.toLineDetailResponse(line, summaries);
    }

    @Override
    public List<MetroLineView> getAllActiveLines() {
        return lineRepository.findAllByIsActive(true).stream().map(this::toLineView).toList();
    }

    @Override
    public MetroLineView getLineById(UUID lineId) {
        Line line = lineRepository.findById(lineId).orElseThrow(() -> new LineNotFoundException(lineId));
        return toLineView(line);
    }

    private MetroLineView toLineView(Line line) {
        return MetroLineView.builder()
                .id(line.getId())
                .code(line.getCode())
                .name(line.getName())
                .active(Boolean.TRUE.equals(line.getIsActive()))
                .build();
    }
}



