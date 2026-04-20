package metro.ExoticStamp.modules.reward.domain.repository;

import metro.ExoticStamp.modules.reward.domain.model.Milestone;
import metro.ExoticStamp.modules.reward.domain.model.PagedSlice;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MilestoneRepository {

    Optional<Milestone> findById(UUID id);

    Milestone save(Milestone milestone);

    List<Milestone> findActiveApplicableToLineAndCampaign(UUID lineId, UUID campaignId);

    PagedSlice<Milestone> findAllPaged(Boolean activeOnly, int page, int size);

    boolean existsById(UUID id);
}
