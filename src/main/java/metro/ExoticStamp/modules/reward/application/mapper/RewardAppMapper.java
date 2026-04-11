package metro.ExoticStamp.modules.reward.application.mapper;

import metro.ExoticStamp.modules.reward.application.view.MilestoneView;
import metro.ExoticStamp.modules.reward.application.view.PartnerView;
import metro.ExoticStamp.modules.reward.application.view.RewardView;
import metro.ExoticStamp.modules.reward.application.view.UserRewardView;
import metro.ExoticStamp.modules.reward.domain.model.Milestone;
import metro.ExoticStamp.modules.reward.domain.model.Partner;
import metro.ExoticStamp.modules.reward.domain.model.Reward;
import metro.ExoticStamp.modules.reward.domain.model.UserReward;
import metro.ExoticStamp.modules.reward.domain.model.VoucherPool;
import org.springframework.stereotype.Component;

@Component
public class RewardAppMapper {

    public PartnerView toPartnerView(Partner p) {
        if (p == null) {
            return null;
        }
        return PartnerView.builder()
                .id(p.getId())
                .name(p.getName())
                .logoUrl(p.getLogoUrl())
                .contactEmail(p.getContactEmail())
                .contractStartDate(p.getContractStartDate())
                .contractEndDate(p.getContractEndDate())
                .active(p.isActive())
                .build();
    }

    public MilestoneView toMilestoneView(Milestone m) {
        if (m == null) {
            return null;
        }
        return MilestoneView.builder()
                .id(m.getId())
                .lineId(m.getLineId())
                .campaignId(m.getCampaignId())
                .stampsRequired(m.getStampsRequired())
                .name(m.getName())
                .description(m.getDescription())
                .active(m.isActive())
                .build();
    }

    public RewardView toRewardView(Reward r) {
        if (r == null) {
            return null;
        }
        return RewardView.builder()
                .id(r.getId())
                .milestoneId(r.getMilestoneId())
                .partnerId(r.getPartnerId())
                .rewardType(r.getRewardType())
                .name(r.getName())
                .description(r.getDescription())
                .valueAmount(r.getValueAmount())
                .expiryDays(r.getExpiryDays())
                .totalStock(r.getTotalStock())
                .issuedCount(r.getIssuedCount() != null ? r.getIssuedCount() : 0)
                .active(r.isActive())
                .build();
    }

    public UserRewardView toUserRewardView(UserReward ur, Reward reward, String voucherCode) {
        if (ur == null) {
            return null;
        }
        return UserRewardView.builder()
                .id(ur.getId())
                .userId(ur.getUserId())
                .rewardId(ur.getRewardId())
                .milestoneId(ur.getMilestoneId())
                .voucherPoolId(ur.getVoucherPoolId())
                .rewardName(reward != null ? reward.getName() : null)
                .rewardType(reward != null ? reward.getRewardType() : null)
                .issuedAt(ur.getIssuedAt())
                .expiresAt(ur.getExpiresAt())
                .redeemedAt(ur.getRedeemedAt())
                .status(ur.getStatus())
                .voucherCode(voucherCode)
                .build();
    }

    public String voucherCodeOrNull(VoucherPool vp) {
        return vp != null ? vp.getCode() : null;
    }
}
