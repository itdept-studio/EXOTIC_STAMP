package metro.ExoticStamp.modules.collection.domain.repository;

import metro.ExoticStamp.modules.collection.domain.model.StampDesign;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StampDesignRepository {

    Optional<StampDesign> findById(UUID id);

    List<StampDesign> findAllByIdIn(Collection<UUID> ids);

    Optional<StampDesign> findActiveByCampaignIdAndStationId(UUID campaignId, UUID stationId);

    List<StampDesign> findActiveByCampaignIdAndStationIdIn(UUID campaignId, Collection<UUID> stationIds);

    StampDesign save(StampDesign stampDesign);

    List<StampDesign> findByCampaignIdOrderByNameAsc(UUID campaignId);
}

