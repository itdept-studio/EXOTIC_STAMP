package metro.ExoticStamp.modules.collection.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.model.PageResult;
import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CampaignRepositoryAdapter implements CampaignRepository {

    private final JpaCampaignRepository jpaCampaignRepository;

    @Override
    public Optional<Campaign> findById(UUID id) {
        return jpaCampaignRepository.findById(id);
    }

    @Override
    public Campaign save(Campaign campaign) {
        return jpaCampaignRepository.save(campaign);
    }

    @Override
    public boolean existsDefaultByLineId(UUID lineId) {
        return jpaCampaignRepository.existsByLineIdAndIsDefaultTrue(lineId);
    }

    @Override
    public Optional<Campaign> findDefaultByLineId(UUID lineId) {
        return jpaCampaignRepository.findByLineIdAndIsDefaultTrueAndIsActiveTrue(lineId);
    }

    @Override
    public List<Campaign> findAllActiveDefaults() {
        return jpaCampaignRepository.findAllByIsDefaultTrueAndIsActiveTrue();
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaCampaignRepository.existsByCode(code);
    }

    @Override
    public PageResult<Campaign> findAllPaged(int page, int size) {
        Page<Campaign> p = jpaCampaignRepository.findAll(PageRequest.of(page, size));
        return PageResult.of(p.getContent(), p.getTotalElements(), p.getTotalPages(), p.getNumber());
    }

    @Override
    public List<Campaign> findByLineIdOrderByCodeAsc(UUID lineId) {
        return jpaCampaignRepository.findByLineIdOrderByCodeAsc(lineId);
    }
}

