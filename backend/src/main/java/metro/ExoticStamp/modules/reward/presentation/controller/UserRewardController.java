package metro.ExoticStamp.modules.reward.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.response.ApiResponse;
import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.common.security.SecurityPrincipalSupport;
import metro.ExoticStamp.modules.reward.application.service.RewardCommandService;
import metro.ExoticStamp.modules.reward.application.service.RewardQueryService;
import metro.ExoticStamp.modules.reward.domain.model.RewardStatus;
import metro.ExoticStamp.modules.reward.presentation.mapper.RewardPresentationMapper;
import metro.ExoticStamp.modules.reward.presentation.request.RedeemRewardRequest;
import metro.ExoticStamp.modules.reward.presentation.response.UserRewardResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rewards")
@RequiredArgsConstructor
@Tag(name = "Rewards")
public class UserRewardController {

    private final RewardQueryService rewardQueryService;
    private final RewardCommandService rewardCommandService;
    private final RewardPresentationMapper presentationMapper;

    @GetMapping("/my")
    @Operation(summary = "List my rewards (paginated)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PageResponse<UserRewardResponse>>> myRewards(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(required = false) RewardStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size
    ) {
        UUID userId = SecurityPrincipalSupport.requireUserId(principal);
        int s = size != null ? size : 0;
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toUserRewardListPage(rewardQueryService.getMyRewards(userId, status, page, s))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reward detail", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<UserRewardResponse>> getById(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID id
    ) {
        UUID userId = SecurityPrincipalSupport.requireUserId(principal);
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toUserRewardDetail(rewardQueryService.getMyRewardDetail(userId, id))));
    }

    @PostMapping("/{id}/redeem")
    @Operation(summary = "Redeem voucher reward", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<UserRewardResponse>> redeem(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) RedeemRewardRequest ignored
    ) {
        UUID userId = SecurityPrincipalSupport.requireUserId(principal);
        rewardCommandService.redeemVoucher(userId, id);
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toUserRewardDetail(rewardQueryService.getMyRewardDetail(userId, id))));
    }
}
