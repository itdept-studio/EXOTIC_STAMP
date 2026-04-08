package metro.ExoticStamp.modules.metro.application;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.metro.application.mapper.MetroAppMapper;
import metro.ExoticStamp.modules.metro.domain.event.LineCreatedEvent;
import metro.ExoticStamp.modules.metro.domain.exception.LineNotFoundException;
import metro.ExoticStamp.modules.metro.domain.model.Line;
import metro.ExoticStamp.modules.metro.domain.repository.LineRepository;
import metro.ExoticStamp.modules.rbac.application.support.RbacTransactionCallbacks;
import metro.ExoticStamp.modules.metro.presentation.dto.request.CreateLineRequest;
import metro.ExoticStamp.modules.metro.presentation.dto.request.ToggleStatusRequest;
import metro.ExoticStamp.modules.metro.presentation.dto.request.UpdateLineRequest;
import metro.ExoticStamp.modules.metro.presentation.dto.response.LineResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LineCommandService {

    private final LineRepository lineRepository;
    private final MetroAppMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public LineResponse createLine(CreateLineRequest req) {
        if (lineRepository.existsByCode(req.getCode())) {
            throw new IllegalArgumentException("Line code already exists: " + req.getCode());
        }
        LocalDateTime now = LocalDateTime.now();
        Line line = Line.builder()
                .code(req.getCode().trim())
                .name(req.getName().trim())
                .color(req.getColor() != null && !req.getColor().isBlank() ? req.getColor() : null)
                .totalStations(0)
                .isActive(true)
                .createdAt(now)
                .build();
        Line saved = lineRepository.save(line);
        RbacTransactionCallbacks.afterCommit(() -> eventPublisher.publishEvent(new LineCreatedEvent(saved.getId())));
        return mapper.toLineResponse(saved);
    }

    @Transactional
    public LineResponse updateLine(UUID lineId, UpdateLineRequest req) {
        Line line = lineRepository.findById(lineId).orElseThrow(() -> new LineNotFoundException(lineId));
        if (req.getCode() != null && !req.getCode().isBlank()) {
            String code = req.getCode().trim();
            if (!code.equals(line.getCode()) && lineRepository.existsByCodeAndIdNot(code, lineId)) {
                throw new IllegalArgumentException("Line code already exists: " + code);
            }
            line.setCode(code);
        }
        if (req.getName() != null && !req.getName().isBlank()) {
            line.setName(req.getName().trim());
        }
        if (req.getColor() != null && !req.getColor().isBlank()) {
            line.setColor(req.getColor());
        }
        if (req.getIsActive() != null) line.setIsActive(req.getIsActive());
        line.setUpdatedAt(LocalDateTime.now());
        return mapper.toLineResponse(lineRepository.save(line));
    }

    @Transactional
    public LineResponse toggleLineStatus(UUID lineId, ToggleStatusRequest req) {
        Line line = lineRepository.findById(lineId).orElseThrow(() -> new LineNotFoundException(lineId));
        line.setIsActive(req.getIsActive());
        line.setUpdatedAt(LocalDateTime.now());
        return mapper.toLineResponse(lineRepository.save(line));
    }
}



