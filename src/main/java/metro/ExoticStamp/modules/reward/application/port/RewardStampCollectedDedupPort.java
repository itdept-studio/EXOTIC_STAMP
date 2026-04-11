package metro.ExoticStamp.modules.reward.application.port;

import java.util.UUID;

public interface RewardStampCollectedDedupPort {

    boolean claimFirstProcessing(UUID eventId);
}
