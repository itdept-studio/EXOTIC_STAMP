package metro.ExoticStamp.modules.collection.infrastructure.repository;

import metro.ExoticStamp.modules.collection.domain.model.UserStamp;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaUserStampRepository extends JpaRepository<UserStamp, UUID> {

    Optional<UserStamp> findByIdempotencyKey(String idempotencyKey);

    boolean existsByUserIdAndStationIdAndCampaignId(UUID userId, UUID stationId, UUID campaignId);

    List<UserStamp> findByUserIdAndCampaignIdOrderByCollectedAtDesc(UUID userId, UUID campaignId);

    List<UserStamp> findByUserIdOrderByCollectedAtDesc(UUID userId, Pageable pageable);

    @Query("select count(distinct us.stationId) from UserStamp us where us.userId = :userId and us.campaignId = :campaignId")
    long countDistinctStationsByUserIdAndCampaignId(
            @Param("userId") UUID userId,
            @Param("campaignId") UUID campaignId
    );
}

