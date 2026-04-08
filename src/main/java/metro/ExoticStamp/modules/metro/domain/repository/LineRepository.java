package metro.ExoticStamp.modules.metro.domain.repository;

import metro.ExoticStamp.modules.metro.domain.model.Line;

import java.util.List;
import java.util.Optional;

public interface LineRepository {

    List<Line> findAll();

    List<Line> findAllByIsActive(boolean active);

    Optional<Line> findById(Integer id);

    Line save(Line line);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Integer id);

    Optional<Line> findByCode(String code);
}
