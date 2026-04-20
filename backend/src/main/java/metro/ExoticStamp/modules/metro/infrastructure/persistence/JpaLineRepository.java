package metro.ExoticStamp.modules.metro.infrastructure.persistence;

import java.util.UUID;

import metro.ExoticStamp.modules.metro.domain.model.Line;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaLineRepository extends JpaRepository<Line, UUID> {

    List<Line> findAllByIsActiveOrderByNameAsc(boolean isActive);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, UUID id);

    Optional<Line> findByCode(String code);
}



