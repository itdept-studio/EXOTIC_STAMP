package metro.ExoticStamp.modules.reward.application.command;

import metro.ExoticStamp.modules.reward.domain.model.RewardType;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateRewardCommand(
        UUID milestoneId,
        UUID partnerId,
        RewardType rewardType,
        String name,
        String description,
        BigDecimal valueAmount,
        Integer expiryDays,
        Integer totalStock
) {
}
