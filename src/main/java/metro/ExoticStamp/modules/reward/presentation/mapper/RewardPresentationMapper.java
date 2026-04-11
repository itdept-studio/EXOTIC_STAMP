package metro.ExoticStamp.modules.reward.presentation.mapper;

import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.modules.reward.application.command.CreateMilestoneCommand;
import metro.ExoticStamp.modules.reward.application.command.CreatePartnerCommand;
import metro.ExoticStamp.modules.reward.application.command.CreateRewardCommand;
import metro.ExoticStamp.modules.reward.application.command.UpdateMilestoneCommand;
import metro.ExoticStamp.modules.reward.application.command.UpdatePartnerCommand;
import metro.ExoticStamp.modules.reward.application.command.UpdateRewardCommand;
import metro.ExoticStamp.modules.reward.application.view.MilestoneView;
import metro.ExoticStamp.modules.reward.application.view.PartnerView;
import metro.ExoticStamp.modules.reward.application.view.RewardView;
import metro.ExoticStamp.modules.reward.application.view.UserRewardView;
import metro.ExoticStamp.modules.reward.application.view.VoucherPoolStatsView;
import metro.ExoticStamp.modules.reward.presentation.request.CreateMilestoneRequest;
import metro.ExoticStamp.modules.reward.presentation.request.CreatePartnerRequest;
import metro.ExoticStamp.modules.reward.presentation.request.CreateRewardRequest;
import metro.ExoticStamp.modules.reward.presentation.request.UpdateMilestoneRequest;
import metro.ExoticStamp.modules.reward.presentation.request.UpdatePartnerRequest;
import metro.ExoticStamp.modules.reward.presentation.request.UpdateRewardRequest;
import metro.ExoticStamp.modules.reward.presentation.response.MilestoneResponse;
import metro.ExoticStamp.modules.reward.presentation.response.PartnerResponse;
import metro.ExoticStamp.modules.reward.presentation.response.RewardResponse;
import metro.ExoticStamp.modules.reward.presentation.response.UserRewardResponse;
import metro.ExoticStamp.modules.reward.presentation.response.VoucherPoolStatsResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RewardPresentationMapper {

    public CreatePartnerCommand toCreatePartnerCommand(CreatePartnerRequest r) {
        return new CreatePartnerCommand(r.getName(), r.getLogoUrl(), r.getContactEmail(),
                r.getContractStartDate(), r.getContractEndDate());
    }

    public UpdatePartnerCommand toUpdatePartnerCommand(UUID id, UpdatePartnerRequest r) {
        return new UpdatePartnerCommand(id, r.getName(), r.getLogoUrl(), r.getContactEmail(),
                r.getContractStartDate(), r.getContractEndDate());
    }

    public PartnerResponse toPartnerResponse(PartnerView v) {
        return PartnerResponse.builder()
                .id(v.id())
                .name(v.name())
                .logoUrl(v.logoUrl())
                .contactEmail(v.contactEmail())
                .contractStartDate(v.contractStartDate())
                .contractEndDate(v.contractEndDate())
                .active(v.active())
                .build();
    }

    public CreateMilestoneCommand toCreateMilestoneCommand(CreateMilestoneRequest r) {
        return new CreateMilestoneCommand(r.getLineId(), r.getCampaignId(), r.getStampsRequired(),
                r.getName(), r.getDescription());
    }

    public UpdateMilestoneCommand toUpdateMilestoneCommand(UUID id, UpdateMilestoneRequest r) {
        return new UpdateMilestoneCommand(id, r.getLineId(), r.getCampaignId(), r.getStampsRequired(),
                r.getName(), r.getDescription());
    }

    public MilestoneResponse toMilestoneResponse(MilestoneView v) {
        return MilestoneResponse.builder()
                .id(v.id())
                .lineId(v.lineId())
                .campaignId(v.campaignId())
                .stampsRequired(v.stampsRequired())
                .name(v.name())
                .description(v.description())
                .active(v.active())
                .build();
    }

    public CreateRewardCommand toCreateRewardCommand(CreateRewardRequest r) {
        return new CreateRewardCommand(r.getMilestoneId(), r.getPartnerId(), r.getRewardType(),
                r.getName(), r.getDescription(), r.getValueAmount(), r.getExpiryDays(), r.getTotalStock());
    }

    public UpdateRewardCommand toUpdateRewardCommand(UUID id, UpdateRewardRequest r) {
        return new UpdateRewardCommand(id, r.getMilestoneId(), r.getPartnerId(), r.getRewardType(),
                r.getName(), r.getDescription(), r.getValueAmount(), r.getExpiryDays(), r.getTotalStock());
    }

    public RewardResponse toRewardResponse(RewardView v) {
        return RewardResponse.builder()
                .id(v.id())
                .milestoneId(v.milestoneId())
                .partnerId(v.partnerId())
                .rewardType(v.rewardType())
                .name(v.name())
                .description(v.description())
                .valueAmount(v.valueAmount())
                .expiryDays(v.expiryDays())
                .totalStock(v.totalStock())
                .issuedCount(v.issuedCount())
                .active(v.active())
                .build();
    }

    public PageResponse<UserRewardResponse> toUserRewardListPage(PageResponse<UserRewardView> page) {
        List<UserRewardResponse> content = page.content().stream()
                .map(this::toUserRewardListItem)
                .collect(Collectors.toList());
        return PageResponse.of(content, page.totalElements(), page.totalPages(), page.page(), page.size());
    }

    public UserRewardResponse toUserRewardListItem(UserRewardView v) {
        return UserRewardResponse.builder()
                .id(v.id())
                .rewardId(v.rewardId())
                .milestoneId(v.milestoneId())
                .rewardName(v.rewardName())
                .rewardType(v.rewardType())
                .issuedAt(v.issuedAt())
                .expiresAt(v.expiresAt())
                .redeemedAt(v.redeemedAt())
                .status(v.status())
                .voucherCode(null)
                .build();
    }

    public UserRewardResponse toUserRewardDetail(UserRewardView v) {
        return UserRewardResponse.builder()
                .id(v.id())
                .rewardId(v.rewardId())
                .milestoneId(v.milestoneId())
                .rewardName(v.rewardName())
                .rewardType(v.rewardType())
                .issuedAt(v.issuedAt())
                .expiresAt(v.expiresAt())
                .redeemedAt(v.redeemedAt())
                .status(v.status())
                .voucherCode(v.voucherCode())
                .build();
    }

    public VoucherPoolStatsResponse toVoucherStatsResponse(VoucherPoolStatsView v) {
        return VoucherPoolStatsResponse.builder()
                .availableCount(v.availableCount())
                .redeemedCount(v.redeemedCount())
                .build();
    }

    public PageResponse<PartnerResponse> toPartnerPage(PageResponse<PartnerView> page) {
        List<PartnerResponse> content = page.content().stream()
                .map(this::toPartnerResponse)
                .collect(Collectors.toList());
        return PageResponse.of(content, page.totalElements(), page.totalPages(), page.page(), page.size());
    }

    public PageResponse<MilestoneResponse> toMilestonePage(PageResponse<MilestoneView> page) {
        List<MilestoneResponse> content = page.content().stream()
                .map(this::toMilestoneResponse)
                .collect(Collectors.toList());
        return PageResponse.of(content, page.totalElements(), page.totalPages(), page.page(), page.size());
    }

    public PageResponse<RewardResponse> toRewardPage(PageResponse<RewardView> page) {
        List<RewardResponse> content = page.content().stream()
                .map(this::toRewardResponse)
                .collect(Collectors.toList());
        return PageResponse.of(content, page.totalElements(), page.totalPages(), page.page(), page.size());
    }
}
