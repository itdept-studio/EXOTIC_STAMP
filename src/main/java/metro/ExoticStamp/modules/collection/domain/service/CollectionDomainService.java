package metro.ExoticStamp.modules.collection.domain.service;

import metro.ExoticStamp.modules.collection.domain.exception.IdempotencyKeyConflictException;
import metro.ExoticStamp.modules.collection.domain.exception.StampAlreadyCollectedException;
import metro.ExoticStamp.modules.collection.domain.model.UserStamp;
import metro.ExoticStamp.modules.collection.domain.repository.UserStampRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class CollectionDomainService {

    private final UserStampRepository userStampRepository;

    public CollectionDomainService(UserStampRepository userStampRepository) {
        this.userStampRepository = userStampRepository;
    }

    public void assertNotAlreadyCollected(UUID userId, UUID stationId, UUID campaignId) {
        if (userStampRepository.existsByUserIdAndStationIdAndCampaignId(userId, stationId, campaignId)) {
            throw new StampAlreadyCollectedException(stationId);
        }
    }

    /**
     * Within the idempotency window: same user gets the existing stamp; another user gets {@link IdempotencyKeyConflictException}.
     */
    public Optional<UserStamp> resolveIdempotentStamp(String idempotencyKey, UUID userId, LocalDateTime since) {
        Optional<UserStamp> existing = userStampRepository
                .findFirstByIdempotencyKeyAndCollectedAtAfterOrderByCollectedAtDesc(idempotencyKey, since);
        if (existing.isEmpty()) {
            return Optional.empty();
        }
        UserStamp stamp = existing.get();
        if (!userId.equals(stamp.getUserId())) {
            throw new IdempotencyKeyConflictException();
        }
        return Optional.of(stamp);
    }
}

