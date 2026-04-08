package metro.ExoticStamp.modules.collection.infrastructure.repository;

import metro.ExoticStamp.modules.collection.domain.model.StampDesign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaStampDesignRepository extends JpaRepository<StampDesign, UUID> {

    Optional<StampDesign> findByCampaignIdAndStationIdAndIsActiveTrue(UUID campaignId, UUID stationId);
}

