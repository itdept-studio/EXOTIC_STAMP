package metro.ExoticStamp.modules.reward.application.view;

import lombok.Builder;
import metro.ExoticStamp.modules.reward.domain.model.RewardStatus;
import metro.ExoticStamp.modules.reward.domain.model.RewardType;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record UserRewardView(
        UUID id,
        UUID userId,
        UUID rewardId,
        UUID milestoneId,
        UUID voucherPoolId,
        String rewardName,
        RewardType rewardType,
        LocalDateTime issuedAt,
        LocalDateTime expiresAt,
        LocalDateTime redeemedAt,
        RewardStatus status,
        String voucherCode
) {
}
