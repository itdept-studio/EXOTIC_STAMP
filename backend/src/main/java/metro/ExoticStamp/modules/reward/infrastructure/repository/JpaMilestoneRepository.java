package metro.ExoticStamp.modules.reward.infrastructure.repository;

import metro.ExoticStamp.modules.reward.domain.model.Milestone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JpaMilestoneRepository extends JpaRepository<Milestone, UUID> {

    @Query("""
            SELECT m FROM Milestone m
            WHERE m.active = true
            AND (m.lineId IS NULL OR m.lineId = :lineId)
            AND (m.campaignId IS NULL OR m.campaignId = :campaignId)
            ORDER BY m.stampsRequired ASC
            """)
    List<Milestone> findActiveApplicableToLineAndCampaign(
            @Param("lineId") UUID lineId,
            @Param("campaignId") UUID campaignId
    );

    Page<Milestone> findByActive(boolean active, Pageable pageable);
}
