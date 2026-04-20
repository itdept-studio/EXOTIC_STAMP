package metro.ExoticStamp.modules.reward.application.service;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.modules.rbac.application.support.RbacTransactionCallbacks;
import metro.ExoticStamp.modules.reward.application.port.RewardCachePort;
import metro.ExoticStamp.modules.reward.application.port.UserStampLineCountPort;
import metro.ExoticStamp.modules.reward.domain.event.RewardIssuedEvent;
import metro.ExoticStamp.modules.reward.domain.exception.MilestoneNotFoundException;
import metro.ExoticStamp.modules.reward.domain.exception.RewardNotFoundException;
import metro.ExoticStamp.modules.reward.domain.exception.RewardNotRedeemableException;
import metro.ExoticStamp.modules.reward.domain.model.Milestone;
import metro.ExoticStamp.modules.reward.domain.model.Reward;
import metro.ExoticStamp.modules.reward.domain.model.RewardStatus;
import metro.ExoticStamp.modules.reward.domain.model.RewardType;
import metro.ExoticStamp.modules.reward.domain.model.UserReward;
import metro.ExoticStamp.modules.reward.domain.model.VoucherPool;
import metro.ExoticStamp.modules.reward.domain.repository.MilestoneRepository;
import metro.ExoticStamp.modules.reward.domain.repository.RewardRepository;
import metro.ExoticStamp.modules.reward.domain.repository.UserRewardRepository;
import metro.ExoticStamp.modules.reward.domain.repository.VoucherPoolRepository;
import metro.ExoticStamp.modules.reward.domain.service.MilestoneDomainService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RewardCommandService {

    private final MilestoneRepository milestoneRepository;
    private final RewardRepository rewardRepository;
    private final UserRewardRepository userRewardRepository;
    private final VoucherPoolRepository voucherPoolRepository;
    private final UserStampLineCountPort userStampLineCountPort;
    private final RewardCachePort rewardCachePort;
    private final ApplicationEventPublisher eventPublisher;
    private final java.time.Clock clock;
    private final MeterRegistry meterRegistry;
    private final MilestoneDomainService milestoneDomainService;

    /**
     * Runs after stamp collection: evaluate milestones and issue rewards (idempotent per milestone).
     */
    @Transactional
    public void handleStampCollected(UUID userId, UUID lineId, UUID campaignId) {
        if (userId == null || lineId == null || campaignId == null) {
            log.warn("[Reward] handleStampCollected skipped: missing userId, lineId, or campaignId");
            return;
        }
        long stampCount = userStampLineCountPort.countDistinctStationsOnLineForUserAndCampaign(userId, lineId, campaignId);
        List<Milestone> applicable = milestoneRepository.findActiveApplicableToLineAndCampaign(lineId, campaignId);
        Set<UUID> rewardedMilestoneIds = userRewardRepository.findMilestoneIdsRewardedForUser(userId);
        List<Milestone> targets = milestoneDomainService.findNewlyCompletedMilestones(
                stampCount,
                applicable,
                rewardedMilestoneIds
        );
        for (Milestone milestone : targets) {
            issueReward(userId, milestone.getId(), lineId);
        }
    }

    /**
     * Issue reward for one milestone if eligible (used by stamp flow and idempotent retries).
     */
    @Transactional
    public void issueReward(UUID userId, UUID milestoneId, UUID lineId) {
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new MilestoneNotFoundException("Milestone not found: " + milestoneId));
        if (!milestone.isActive()) {
            log.debug("[Reward] skip issue: milestone inactive milestoneId={}", milestoneId);
            return;
        }
        if (userRewardRepository.existsByUserIdAndMilestoneId(userId, milestoneId)) {
            return;
        }
        Reward reward = rewardRepository.findActiveByMilestoneId(milestoneId)
                .orElse(null);
        if (reward == null) {
            log.debug("[Reward] skip issue: no active reward for milestoneId={}", milestoneId);
            return;
        }
        if (!reward.isActive()) {
            return;
        }

        if (!rewardRepository.incrementIssuedCountIfStockAllows(reward.getId())) {
            log.warn("[Reward] skip issue: stock exhausted rewardId={}", reward.getId());
            return;
        }

        UUID voucherPoolId = null;
        if (reward.getRewardType() == RewardType.VOUCHER) {
            Optional<VoucherPool> locked = voucherPoolRepository.lockNextAvailableForReward(reward.getId());
            if (locked.isEmpty()) {
                log.warn("[Reward] voucher pool empty for rewardId={}, issuing without code", reward.getId());
            } else {
                VoucherPool vp = locked.get();
                voucherPoolId = vp.getId();
            }
        }

        LocalDateTime issuedAt = LocalDateTime.now(clock);
        LocalDateTime expiresAt = null;
        if (reward.getExpiryDays() != null && reward.getExpiryDays() > 0) {
            expiresAt = issuedAt.plusDays(reward.getExpiryDays());
        }

        UserReward toSave = UserReward.builder()
                .userId(userId)
                .rewardId(reward.getId())
                .milestoneId(milestoneId)
                .voucherPoolId(voucherPoolId)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .status(RewardStatus.ISSUED)
                .build();

        try {
            UserReward saved = userRewardRepository.save(toSave);
            meterRegistry.counter("reward.issued", "rewardType", reward.getRewardType().name()).increment();
            rewardCachePort.evictUserRewardListAll(userId);
            rewardCachePort.evictUserRewardDetail(userId, saved.getId());
            UUID rewardId = reward.getId();
            RewardType rewardType = reward.getRewardType();
            RbacTransactionCallbacks.afterCommit(() -> {
                try {
                    eventPublisher.publishEvent(new RewardIssuedEvent(userId, rewardId, milestoneId, rewardType, lineId));
                } catch (Exception e) {
                    log.error("[Reward] RewardIssuedEvent publish failed userId={} rewardId={}: {}",
                            userId, rewardId, e.getMessage(), e);
                }
            });
        } catch (DataIntegrityViolationException ex) {
            if (isUserRewardUniqueViolation(ex)) {
                rewardRepository.decrementIssuedCount(reward.getId());
                log.debug("[Reward] duplicate user_reward skipped userId={} milestoneId={}", userId, milestoneId);
                return;
            }
            rewardRepository.decrementIssuedCount(reward.getId());
            throw ex;
        }
    }

    @Transactional
    public void redeemVoucher(UUID userId, UUID userRewardId) {
        UserReward ur = userRewardRepository.findByUserIdAndId(userId, userRewardId)
                .orElseThrow(() -> new RewardNotFoundException("User reward not found: " + userRewardId));
        LocalDateTime now = LocalDateTime.now(clock);
        if (ur.getStatus() != RewardStatus.ISSUED) {
            throw new RewardNotRedeemableException("Reward cannot be redeemed in status: " + ur.getStatus());
        }
        if (ur.getExpiresAt() != null && ur.getExpiresAt().isBefore(now)) {
            ur.setStatus(RewardStatus.EXPIRED);
            userRewardRepository.save(ur);
            rewardCachePort.evictUserRewardListAll(userId);
            rewardCachePort.evictUserRewardDetail(userId, userRewardId);
            throw new RewardNotRedeemableException("Reward cannot be redeemed in status: EXPIRED");
        }
        ur.setStatus(RewardStatus.REDEEMED);
        ur.setRedeemedAt(now);
        userRewardRepository.save(ur);
        if (ur.getVoucherPoolId() != null) {
            voucherPoolRepository.findById(ur.getVoucherPoolId()).ifPresent(vp -> {
                vp.setRedeemed(true);
                voucherPoolRepository.save(vp);
            });
        }
        rewardCachePort.evictUserRewardListAll(userId);
        rewardCachePort.evictUserRewardDetail(userId, userRewardId);
    }

    private static boolean isUserRewardUniqueViolation(DataIntegrityViolationException ex) {
        String msg = ex.getMostSpecificCause().getMessage();
        if (msg == null) {
            return false;
        }
        return msg.contains("uq_user_rewards_once");
    }
}
