package metro.ExoticStamp.modules.collection.infrastructure.repository;

import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaCampaignRepository extends JpaRepository<Campaign, UUID> {

    boolean existsByLineIdAndIsDefaultTrue(UUID lineId);

    Optional<Campaign> findByLineIdAndIsDefaultTrueAndIsActiveTrue(UUID lineId);

    List<Campaign> findAllByIsDefaultTrueAndIsActiveTrue();

    boolean existsByCode(String code);

    Page<Campaign> findAll(Pageable pageable);

    List<Campaign> findByLineIdOrderByCodeAsc(UUID lineId);
}

