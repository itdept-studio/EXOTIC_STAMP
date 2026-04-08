package metro.ExoticStamp.modules.collection.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.collection.domain.model.UserStamp;
import metro.ExoticStamp.modules.collection.domain.repository.UserStampRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserStampRepositoryAdapter implements UserStampRepository {

    private final JpaUserStampRepository jpaUserStampRepository;

    @Override
    public Optional<UserStamp> findById(UUID id) {
        return jpaUserStampRepository.findById(id);
    }

    @Override
    public Optional<UserStamp> findByIdempotencyKey(String idempotencyKey) {
        return jpaUserStampRepository.findByIdempotencyKey(idempotencyKey);
    }

    @Override
    public boolean existsByUserIdAndStationIdAndCampaignId(UUID userId, UUID stationId, UUID campaignId) {
        return jpaUserStampRepository.existsByUserIdAndStationIdAndCampaignId(userId, stationId, campaignId);
    }

    @Override
    public UserStamp save(UserStamp userStamp) {
        return jpaUserStampRepository.save(userStamp);
    }

    @Override
    public List<UserStamp> findRecentByUserId(UUID userId, int limit) {
        return jpaUserStampRepository.findByUserIdOrderByCollectedAtDesc(userId, PageRequest.of(0, limit));
    }

    @Override
    public List<UserStamp> findByUserIdAndCampaignId(UUID userId, UUID campaignId) {
        return jpaUserStampRepository.findByUserIdAndCampaignIdOrderByCollectedAtDesc(userId, campaignId);
    }

    @Override
    public long countDistinctStationsByUserIdAndCampaignId(UUID userId, UUID campaignId) {
        return jpaUserStampRepository.countDistinctStationsByUserIdAndCampaignId(userId, campaignId);
    }
}

