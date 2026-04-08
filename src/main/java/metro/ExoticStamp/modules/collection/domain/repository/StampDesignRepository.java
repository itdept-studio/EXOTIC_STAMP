package metro.ExoticStamp.modules.collection.domain.repository;

import metro.ExoticStamp.modules.collection.domain.model.StampDesign;

import java.util.Optional;
import java.util.UUID;

public interface StampDesignRepository {

    Optional<StampDesign> findById(UUID id);

    Optional<StampDesign> findActiveByCampaignIdAndStationId(UUID campaignId, UUID stationId);

    StampDesign save(StampDesign stampDesign);
}

