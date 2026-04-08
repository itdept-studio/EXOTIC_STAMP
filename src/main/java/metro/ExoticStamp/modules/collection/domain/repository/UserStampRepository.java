package metro.ExoticStamp.modules.collection.domain.repository;

import metro.ExoticStamp.modules.collection.domain.model.UserStamp;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserStampRepository {

    Optional<UserStamp> findById(UUID id);

    Optional<UserStamp> findByIdempotencyKey(String idempotencyKey);

    boolean existsByUserIdAndStationIdAndCampaignId(UUID userId, UUID stationId, UUID campaignId);

    UserStamp save(UserStamp userStamp);

    List<UserStamp> findRecentByUserId(UUID userId, int limit);

    List<UserStamp> findByUserIdAndCampaignId(UUID userId, UUID campaignId);

    long countDistinctStationsByUserIdAndCampaignId(UUID userId, UUID campaignId);
}

