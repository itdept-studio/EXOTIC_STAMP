package metro.ExoticStamp.modules.reward.application.service;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.modules.reward.application.mapper.RewardAppMapper;
import metro.ExoticStamp.modules.reward.application.port.RewardCachePort;
import metro.ExoticStamp.modules.reward.application.view.UserRewardView;
import metro.ExoticStamp.modules.reward.config.RewardProperties;
import metro.ExoticStamp.modules.reward.domain.exception.RewardNotFoundException;
import metro.ExoticStamp.modules.reward.domain.model.PagedSlice;
import metro.ExoticStamp.modules.reward.domain.model.Reward;
import metro.ExoticStamp.modules.reward.domain.model.RewardStatus;
import metro.ExoticStamp.modules.reward.domain.model.UserReward;
import metro.ExoticStamp.modules.reward.domain.repository.RewardRepository;
import metro.ExoticStamp.modules.reward.domain.repository.UserRewardRepository;
import metro.ExoticStamp.modules.reward.domain.repository.VoucherPoolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RewardQueryService {

    private final UserRewardRepository userRewardRepository;
    private final RewardRepository rewardRepository;
    private final VoucherPoolRepository voucherPoolRepository;
    private final RewardCachePort rewardCachePort;
    private final RewardAppMapper rewardAppMapper;
    private final RewardProperties rewardProperties;

    public PageResponse<UserRewardView> getMyRewards(UUID userId, RewardStatus status, int page, int size) {
        int p = Math.max(0, page);
        int s = normalizeSize(size);
        if (status == null) {
            Optional<PageResponse<UserRewardView>> cached = rewardCachePort.getUserRewardList(userId, p, s);
            if (cached.isPresent()) {
                return cached.get();
            }
        }
        PagedSlice<UserReward> slice = status == null
                ? userRewardRepository.findByUserIdOrderByIssuedAtDesc(userId, p, s)
                : userRewardRepository.findByUserIdAndStatusOrderByIssuedAtDesc(userId, status, p, s);
        Map<UUID, Reward> rewardMap = loadRewards(slice.content());
        List<UserRewardView> content = slice.content().stream()
                .map(ur -> rewardAppMapper.toUserRewardView(ur, rewardMap.get(ur.getRewardId()), null))
                .collect(Collectors.toList());
        PageResponse<UserRewardView> res = PageResponse.of(
                content,
                slice.totalElements(),
                slice.totalPages(),
                slice.page(),
                slice.size()
        );
        if (status == null) {
            rewardCachePort.putUserRewardList(userId, p, s, res);
        }
        return res;
    }

    public UserRewardView getMyRewardDetail(UUID userId, UUID userRewardId) {
        Optional<UserRewardView> cached = rewardCachePort.getUserRewardDetail(userId, userRewardId);
        if (cached.isPresent()) {
            return cached.get();
        }
        UserReward ur = userRewardRepository.findByUserIdAndId(userId, userRewardId)
                .orElseThrow(() -> new RewardNotFoundException("User reward not found: " + userRewardId));
        Reward reward = rewardRepository.findById(ur.getRewardId()).orElse(null);
        String voucherCode = null;
        if (ur.getStatus() == RewardStatus.ISSUED || ur.getStatus() == RewardStatus.REDEEMED) {
            if (ur.getVoucherPoolId() != null) {
                voucherCode = voucherPoolRepository.findById(ur.getVoucherPoolId())
                        .map(vp -> vp.getCode())
                        .orElse(null);
            }
        }
        UserRewardView view = rewardAppMapper.toUserRewardView(ur, reward, voucherCode);
        rewardCachePort.putUserRewardDetail(userId, userRewardId, view);
        return view;
    }

    private Map<UUID, Reward> loadRewards(List<UserReward> list) {
        Set<UUID> ids = list.stream().map(UserReward::getRewardId).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<UUID, Reward> map = new HashMap<>();
        rewardRepository.findAllByIds(ids).forEach(r -> map.put(r.getId(), r));
        return map;
    }

    private int normalizeSize(int size) {
        int max = rewardProperties.getMaxPageSize();
        int def = rewardProperties.getDefaultPageSize();
        if (size <= 0) {
            return def;
        }
        return Math.min(size, max);
    }
}
