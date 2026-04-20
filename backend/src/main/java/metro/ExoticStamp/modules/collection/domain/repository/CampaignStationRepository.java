package metro.ExoticStamp.modules.collection.domain.repository;

import java.util.UUID;

public interface CampaignStationRepository {

    void assign(UUID campaignId, UUID stationId);

    void remove(UUID campaignId, UUID stationId);

    boolean exists(UUID campaignId, UUID stationId);
}
