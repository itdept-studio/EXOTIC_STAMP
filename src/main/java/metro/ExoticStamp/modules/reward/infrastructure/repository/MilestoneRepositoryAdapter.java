package metro.ExoticStamp.modules.reward.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.reward.domain.model.Milestone;
import metro.ExoticStamp.modules.reward.domain.model.PagedSlice;
import metro.ExoticStamp.modules.reward.domain.repository.MilestoneRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MilestoneRepositoryAdapter implements MilestoneRepository {

    private final JpaMilestoneRepository jpaMilestoneRepository;

    @Override
    public Optional<Milestone> findById(UUID id) {
        return jpaMilestoneRepository.findById(id);
    }

    @Override
    public Milestone save(Milestone milestone) {
        return jpaMilestoneRepository.save(milestone);
    }

    @Override
    public List<Milestone> findActiveApplicableToLineAndCampaign(UUID lineId, UUID campaignId) {
        return jpaMilestoneRepository.findActiveApplicableToLineAndCampaign(lineId, campaignId);
    }

    @Override
    public PagedSlice<Milestone> findAllPaged(Boolean activeOnly, int page, int size) {
        PageRequest pr = PageRequest.of(page, size);
        Page<Milestone> p;
        if (activeOnly == null) {
            p = jpaMilestoneRepository.findAll(pr);
        } else {
            p = jpaMilestoneRepository.findByActive(activeOnly, pr);
        }
        return new PagedSlice<>(p.getContent(), p.getTotalElements(), p.getTotalPages(), p.getNumber(), p.getSize());
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaMilestoneRepository.existsById(id);
    }
}
