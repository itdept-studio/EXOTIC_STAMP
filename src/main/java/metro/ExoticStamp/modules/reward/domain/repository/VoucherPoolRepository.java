package metro.ExoticStamp.modules.reward.domain.repository;

import metro.ExoticStamp.modules.reward.domain.model.VoucherPool;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VoucherPoolRepository {

    List<VoucherPool> saveAll(Iterable<VoucherPool> vouchers);

    VoucherPool save(VoucherPool voucher);

    Optional<VoucherPool> findById(UUID id);

    /**
     * Lock one unredeemed row for the reward, or empty if none (SKIP LOCKED).
     */
    Optional<VoucherPool> lockNextAvailableForReward(UUID rewardId);

    long countAvailableByRewardId(UUID rewardId);

    long countRedeemedByRewardId(UUID rewardId);
}
