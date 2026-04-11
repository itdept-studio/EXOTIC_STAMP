package metro.ExoticStamp.modules.reward.domain.event;

import metro.ExoticStamp.modules.reward.domain.model.RewardType;

import java.util.UUID;

/**
 * Published after successful user reward issuance (after transaction commit). Immutable POJO.
 */
public final class RewardIssuedEvent {

    private final UUID userId;
    private final UUID rewardId;
    private final UUID milestoneId;
    private final RewardType rewardType;
    private final UUID lineId;

    public RewardIssuedEvent(UUID userId, UUID rewardId, UUID milestoneId, RewardType rewardType, UUID lineId) {
        this.userId = userId;
        this.rewardId = rewardId;
        this.milestoneId = milestoneId;
        this.rewardType = rewardType;
        this.lineId = lineId;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getRewardId() {
        return rewardId;
    }

    public UUID getMilestoneId() {
        return milestoneId;
    }

    public RewardType getRewardType() {
        return rewardType;
    }

    public UUID getLineId() {
        return lineId;
    }
}
