package metro.ExoticStamp.modules.collection.domain.service;

import metro.ExoticStamp.modules.collection.domain.exception.StampAlreadyCollectedException;
import metro.ExoticStamp.modules.collection.domain.repository.UserStampRepository;
import org.springframework.stereotype.Service;

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
}

