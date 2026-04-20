package metro.ExoticStamp.modules.metro.application;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.metro.application.mapper.MetroAppMapper;
import metro.ExoticStamp.modules.metro.application.port.LineReadPort;
import metro.ExoticStamp.modules.metro.application.view.LineDetailView;
import metro.ExoticStamp.modules.metro.application.view.LineView;
import metro.ExoticStamp.modules.metro.application.view.MetroLineView;
import metro.ExoticStamp.modules.metro.application.view.StationView;
import metro.ExoticStamp.modules.metro.domain.exception.LineNotFoundException;
import metro.ExoticStamp.modules.metro.domain.model.Line;
import metro.ExoticStamp.modules.metro.domain.model.Station;
import metro.ExoticStamp.modules.metro.domain.repository.LineRepository;
import metro.ExoticStamp.modules.metro.domain.repository.StationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LineQueryService implements LineReadPort {

    private final LineRepository lineRepository;
    private final StationRepository stationRepository;
    private final MetroAppMapper mapper;

    public List<LineView> getAllLines(boolean activeOnly) {
        List<Line> lines = activeOnly
                ? lineRepository.findAllByIsActive(true)
                : lineRepository.findAll();
        return lines.stream().map(mapper::toLineView).toList();
    }

    public LineDetailView getLineDetail(UUID lineId, boolean stationsActiveOnly) {
        Line line = lineRepository.findById(lineId).orElseThrow(() -> new LineNotFoundException(lineId));
        List<Station> stations = stationsActiveOnly
                ? stationRepository.findAllByLineIdAndIsActive(lineId, true)
                : stationRepository.findAllByLineId(lineId);
        List<StationView> summaries = stations.stream().map(mapper::toStationView).toList();
        return mapper.toLineDetailView(line, summaries);
    }

    @Override
    public List<MetroLineView> getAllActiveLines() {
        return lineRepository.findAllByIsActive(true).stream().map(this::toSharedLineView).toList();
    }

    @Override
    public MetroLineView getLineById(UUID lineId) {
        Line line = lineRepository.findById(lineId).orElseThrow(() -> new LineNotFoundException(lineId));
        return toSharedLineView(line);
    }

    private MetroLineView toSharedLineView(Line line) {
        return MetroLineView.builder()
                .id(line.getId())
                .code(line.getCode())
                .name(line.getName())
                .active(Boolean.TRUE.equals(line.getIsActive()))
                .build();
    }
}
