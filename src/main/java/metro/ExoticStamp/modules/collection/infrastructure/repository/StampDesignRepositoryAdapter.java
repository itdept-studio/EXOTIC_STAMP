package metro.ExoticStamp.modules.collection.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.collection.domain.model.StampDesign;
import metro.ExoticStamp.modules.collection.domain.repository.StampDesignRepository;
import org.springframework.stereotype.Component;

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
    public Optional<StampDesign> findActiveByCampaignIdAndStationId(UUID campaignId, UUID stationId) {
        return jpaStampDesignRepository.findByCampaignIdAndStationIdAndIsActiveTrue(campaignId, stationId);
    }

    @Override
    public StampDesign save(StampDesign stampDesign) {
        return jpaStampDesignRepository.save(stampDesign);
    }
}

