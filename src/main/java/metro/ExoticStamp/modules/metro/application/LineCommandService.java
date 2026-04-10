package metro.ExoticStamp.modules.metro.application;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.metro.application.command.CreateLineCommand;
import metro.ExoticStamp.modules.metro.application.command.ToggleLineStatusCommand;
import metro.ExoticStamp.modules.metro.application.command.UpdateLineCommand;
import metro.ExoticStamp.modules.metro.application.mapper.MetroAppMapper;
import metro.ExoticStamp.modules.metro.application.view.LineView;
import metro.ExoticStamp.modules.metro.domain.event.LineCreatedEvent;
import metro.ExoticStamp.modules.metro.domain.exception.LineNotFoundException;
import metro.ExoticStamp.modules.metro.domain.model.Line;
import metro.ExoticStamp.modules.metro.domain.repository.LineRepository;
import metro.ExoticStamp.modules.rbac.application.support.RbacTransactionCallbacks;
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
    public LineView createLine(CreateLineCommand command) {
        if (lineRepository.existsByCode(command.getCode())) {
            throw new IllegalArgumentException("Line code already exists: " + command.getCode());
        }
        LocalDateTime now = LocalDateTime.now();
        Line line = Line.builder()
                .code(command.getCode().trim())
                .name(command.getName().trim())
                .color(command.getColor() != null && !command.getColor().isBlank() ? command.getColor() : null)
                .totalStations(0)
                .isActive(true)
                .createdAt(now)
                .build();
        Line saved = lineRepository.save(line);
        RbacTransactionCallbacks.afterCommit(() -> eventPublisher.publishEvent(new LineCreatedEvent(saved.getId())));
        return mapper.toLineView(saved);
    }

    @Transactional
    public LineView updateLine(UpdateLineCommand command) {
        Line line = lineRepository.findById(command.getLineId())
                .orElseThrow(() -> new LineNotFoundException(command.getLineId()));
        if (command.getCode() != null && !command.getCode().isBlank()) {
            String code = command.getCode().trim();
            if (!code.equals(line.getCode()) && lineRepository.existsByCodeAndIdNot(code, command.getLineId())) {
                throw new IllegalArgumentException("Line code already exists: " + code);
            }
            line.setCode(code);
        }
        if (command.getName() != null && !command.getName().isBlank()) {
            line.setName(command.getName().trim());
        }
        if (command.getColor() != null && !command.getColor().isBlank()) {
            line.setColor(command.getColor());
        }
        if (command.getIsActive() != null) {
            line.setIsActive(command.getIsActive());
        }
        line.setUpdatedAt(LocalDateTime.now());
        return mapper.toLineView(lineRepository.save(line));
    }

    @Transactional
    public LineView toggleLineStatus(ToggleLineStatusCommand command) {
        Line line = lineRepository.findById(command.getLineId())
                .orElseThrow(() -> new LineNotFoundException(command.getLineId()));
        line.setIsActive(command.getIsActive());
        line.setUpdatedAt(LocalDateTime.now());
        return mapper.toLineView(lineRepository.save(line));
    }
}
