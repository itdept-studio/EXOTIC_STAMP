package metro.ExoticStamp.modules.metro.infrastructure.persistence;

import metro.ExoticStamp.modules.metro.domain.model.Line;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaLineRepository extends JpaRepository<Line, Integer> {

    List<Line> findAllByIsActiveOrderByNameAsc(boolean isActive);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Integer id);

    Optional<Line> findByCode(String code);
}
