package metro.ExoticStamp.modules.reward.presentation.response;

import lombok.Builder;
import metro.ExoticStamp.modules.reward.domain.model.RewardStatus;
import metro.ExoticStamp.modules.reward.domain.model.RewardType;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record UserRewardResponse(
        UUID id,
        UUID rewardId,
        UUID milestoneId,
        String rewardName,
        RewardType rewardType,
        LocalDateTime issuedAt,
        LocalDateTime expiresAt,
        LocalDateTime redeemedAt,
        RewardStatus status,
        String voucherCode
) {
}
