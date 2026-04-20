package metro.ExoticStamp.modules.collection.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.collection.domain.model.StampDesign;
import metro.ExoticStamp.modules.collection.domain.repository.StampDesignRepository;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StampDesignRepositoryAdapter implements StampDesignRepository {

    private final JpaStampDesignRepository jpaStampDesignRepository;

    @Override
    public Optional<StampDesign> findById(UUID id) {
        return jpaStampDesignRepository.findById(id);
    }

    @Override
    public List<StampDesign> findAllByIdIn(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return jpaStampDesignRepository.findAllById(ids);
    }

    @Override
    public Optional<StampDesign> findActiveByCampaignIdAndStationId(UUID campaignId, UUID stationId) {
        return jpaStampDesignRepository.findByCampaignIdAndStationIdAndIsActiveTrue(campaignId, stationId);
    }

    @Override
    public List<StampDesign> findActiveByCampaignIdAndStationIdIn(UUID campaignId, Collection<UUID> stationIds) {
        if (stationIds == null || stationIds.isEmpty()) {
            return List.of();
        }
        return jpaStampDesignRepository.findByCampaignIdAndStationIdInAndIsActiveTrue(campaignId, stationIds);
    }

    @Override
    public StampDesign save(StampDesign stampDesign) {
        return jpaStampDesignRepository.save(stampDesign);
    }

    @Override
    public List<StampDesign> findByCampaignIdOrderByNameAsc(UUID campaignId) {
        return jpaStampDesignRepository.findByCampaignIdOrderByNameAsc(campaignId);
    }
}

