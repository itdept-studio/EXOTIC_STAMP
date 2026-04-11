package metro.ExoticStamp.modules.reward.infrastructure.repository;

import metro.ExoticStamp.modules.reward.domain.model.VoucherPool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JpaVoucherPoolRepository extends JpaRepository<VoucherPool, UUID> {

    @Query(value = """
            SELECT COUNT(*) FROM voucher_pool vp
            WHERE vp.reward_id = :rewardId AND vp.is_redeemed = FALSE
            AND NOT EXISTS (SELECT 1 FROM user_rewards ur WHERE ur.voucher_pool_id = vp.id)
            """, nativeQuery = true)
    long countUnissuedAvailableByRewardId(@Param("rewardId") UUID rewardId);

    @Query(value = """
            SELECT COUNT(*) FROM voucher_pool vp
            WHERE vp.reward_id = :rewardId AND vp.is_redeemed = TRUE
            """, nativeQuery = true)
    long countRedeemedByRewardId(@Param("rewardId") UUID rewardId);

    @Query(value = """
            SELECT vp.* FROM voucher_pool vp
            WHERE vp.reward_id = :rewardId AND vp.is_redeemed = FALSE
            AND NOT EXISTS (SELECT 1 FROM user_rewards ur WHERE ur.voucher_pool_id = vp.id)
            ORDER BY vp.created_at ASC
            LIMIT 1
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    Optional<VoucherPool> lockNextAvailable(@Param("rewardId") UUID rewardId);
}
