package metro.ExoticStamp.modules.collection.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaCampaignStationRepository extends JpaRepository<CampaignStationEntity, UUID> {

    boolean existsByCampaignIdAndStationId(UUID campaignId, UUID stationId);

    void deleteByCampaignIdAndStationId(UUID campaignId, UUID stationId);
}
