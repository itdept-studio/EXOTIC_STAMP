package metro.ExoticStamp.modules.metro.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.metro.domain.model.Line;
import metro.ExoticStamp.modules.metro.domain.repository.LineRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LineRepositoryAdapter implements LineRepository {

    private final JpaLineRepository jpaLineRepository;

    @Override
    public List<Line> findAll() {
        return jpaLineRepository.findAll();
    }

    @Override
    public List<Line> findAllByIsActive(boolean active) {
        return jpaLineRepository.findAllByIsActiveOrderByNameAsc(active);
    }

    @Override
    public Optional<Line> findById(Integer id) {
        return jpaLineRepository.findById(id);
    }

    @Override
    public Line save(Line line) {
        return jpaLineRepository.save(line);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaLineRepository.existsByCode(code);
    }

    @Override
    public boolean existsByCodeAndIdNot(String code, Integer id) {
        return jpaLineRepository.existsByCodeAndIdNot(code, id);
    }

    @Override
    public Optional<Line> findByCode(String code) {
        return jpaLineRepository.findByCode(code);
    }
}
