package metro.ExoticStamp.modules.reward.application.service;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import metro.ExoticStamp.modules.reward.application.port.RewardCachePort;
import metro.ExoticStamp.modules.reward.application.port.UserStampLineCountPort;
import metro.ExoticStamp.modules.reward.domain.event.RewardIssuedEvent;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RewardCommandServiceTest {

    @Mock
    private MilestoneRepository milestoneRepository;
    @Mock
    private RewardRepository rewardRepository;
    @Mock
    private UserRewardRepository userRewardRepository;
    @Mock
    private VoucherPoolRepository voucherPoolRepository;
    @Mock
    private UserStampLineCountPort userStampLineCountPort;
    @Mock
    private RewardCachePort rewardCachePort;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private Clock clock;
    private RewardCommandService service;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-04-12T10:00:00Z"), ZoneOffset.UTC);
        service = new RewardCommandService(
                milestoneRepository,
                rewardRepository,
                userRewardRepository,
                voucherPoolRepository,
                userStampLineCountPort,
                rewardCachePort,
                eventPublisher,
                clock,
                new SimpleMeterRegistry(),
                new MilestoneDomainService()
        );
    }

    @Test
    void redeemVoucher_notFound_throws() {
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        when(userRewardRepository.findByUserIdAndId(userId, id)).thenReturn(Optional.empty());
        assertThrows(RewardNotFoundException.class, () -> service.redeemVoucher(userId, id));
    }

    @Test
    void redeemVoucher_notIssued_throws() {
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        UserReward ur = UserReward.builder()
                .id(id)
                .userId(userId)
                .rewardId(UUID.randomUUID())
                .milestoneId(UUID.randomUUID())
                .issuedAt(LocalDateTime.now(clock))
                .status(RewardStatus.EXPIRED)
                .build();
        when(userRewardRepository.findByUserIdAndId(userId, id)).thenReturn(Optional.of(ur));
        assertThrows(RewardNotRedeemableException.class, () -> service.redeemVoucher(userId, id));
    }

    @Test
    void redeemVoucher_expiredIssued_markExpiredAndThrows() {
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        UserReward ur = UserReward.builder()
                .id(id)
                .userId(userId)
                .rewardId(UUID.randomUUID())
                .milestoneId(UUID.randomUUID())
                .issuedAt(LocalDateTime.now(clock).minusDays(10))
                .expiresAt(LocalDateTime.now(clock).minusMinutes(1))
                .status(RewardStatus.ISSUED)
                .build();
        when(userRewardRepository.findByUserIdAndId(userId, id)).thenReturn(Optional.of(ur));

        assertThrows(RewardNotRedeemableException.class, () -> service.redeemVoucher(userId, id));
        assertEquals(RewardStatus.EXPIRED, ur.getStatus());
        verify(userRewardRepository).save(ur);
    }

    @Test
    void issueReward_swallowsUniqueViolationAndDecrements() {
        UUID userId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        UUID milestoneId = UUID.randomUUID();
        UUID rewardId = UUID.randomUUID();

        Milestone m = Milestone.builder()
                .id(milestoneId)
                .stampsRequired(1)
                .name("M")
                .active(true)
                .build();
        Reward r = Reward.builder()
                .id(rewardId)
                .milestoneId(milestoneId)
                .rewardType(RewardType.DIGITAL_STICKER)
                .name("R")
                .issuedCount(0)
                .active(true)
                .build();

        when(milestoneRepository.findById(milestoneId)).thenReturn(Optional.of(m));
        when(userRewardRepository.existsByUserIdAndMilestoneId(userId, milestoneId)).thenReturn(false);
        when(rewardRepository.findActiveByMilestoneId(milestoneId)).thenReturn(Optional.of(r));
        when(rewardRepository.incrementIssuedCountIfStockAllows(rewardId)).thenReturn(true);
        when(userRewardRepository.save(any(UserReward.class))).thenThrow(new DataIntegrityViolationException("uq_user_rewards_once"));

        TransactionSynchronizationManager.initSynchronization();
        try {
            service.issueReward(userId, milestoneId, lineId);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }

        verify(rewardRepository).decrementIssuedCount(rewardId);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void issueReward_publishesEventAfterCommit() {
        UUID userId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        UUID milestoneId = UUID.randomUUID();
        UUID rewardId = UUID.randomUUID();

        Milestone m = Milestone.builder()
                .id(milestoneId)
                .stampsRequired(1)
                .name("M")
                .active(true)
                .build();
        Reward r = Reward.builder()
                .id(rewardId)
                .milestoneId(milestoneId)
                .rewardType(RewardType.BONUS_STAMP)
                .name("R")
                .issuedCount(0)
                .active(true)
                .build();
        UserReward saved = UserReward.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .rewardId(rewardId)
                .milestoneId(milestoneId)
                .issuedAt(LocalDateTime.now(clock))
                .status(RewardStatus.ISSUED)
                .build();

        when(milestoneRepository.findById(milestoneId)).thenReturn(Optional.of(m));
        when(userRewardRepository.existsByUserIdAndMilestoneId(userId, milestoneId)).thenReturn(false);
        when(rewardRepository.findActiveByMilestoneId(milestoneId)).thenReturn(Optional.of(r));
        when(rewardRepository.incrementIssuedCountIfStockAllows(rewardId)).thenReturn(true);
        when(userRewardRepository.save(any(UserReward.class))).thenReturn(saved);

        TransactionSynchronizationManager.initSynchronization();
        try {
            service.issueReward(userId, milestoneId, lineId);
            TransactionSynchronizationManager.getSynchronizations().forEach(s -> s.afterCommit());
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }

        ArgumentCaptor<RewardIssuedEvent> cap = ArgumentCaptor.forClass(RewardIssuedEvent.class);
        verify(eventPublisher).publishEvent(cap.capture());
        assertEquals(userId, cap.getValue().getUserId());
        assertEquals(rewardId, cap.getValue().getRewardId());
    }

    @Test
    void handleStampCollected_skipsWhenStockExhausted() {
        UUID userId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        UUID campaignId = UUID.randomUUID();
        UUID milestoneId = UUID.randomUUID();
        UUID rewardId = UUID.randomUUID();

        when(userStampLineCountPort.countDistinctStationsOnLineForUserAndCampaign(userId, lineId, campaignId)).thenReturn(5L);
        Milestone m = Milestone.builder()
                .id(milestoneId)
                .lineId(lineId)
                .campaignId(campaignId)
                .stampsRequired(3)
                .name("M")
                .active(true)
                .build();
        when(milestoneRepository.findActiveApplicableToLineAndCampaign(lineId, campaignId)).thenReturn(java.util.List.of(m));
        when(userRewardRepository.findMilestoneIdsRewardedForUser(userId)).thenReturn(Set.of());
        Reward r = Reward.builder()
                .id(rewardId)
                .milestoneId(milestoneId)
                .rewardType(RewardType.DIGITAL_STICKER)
                .name("R")
                .active(true)
                .build();
        when(milestoneRepository.findById(milestoneId)).thenReturn(Optional.of(m));
        when(userRewardRepository.existsByUserIdAndMilestoneId(userId, milestoneId)).thenReturn(false);
        when(rewardRepository.findActiveByMilestoneId(milestoneId)).thenReturn(Optional.of(r));
        when(rewardRepository.incrementIssuedCountIfStockAllows(rewardId)).thenReturn(false);

        service.handleStampCollected(userId, lineId, campaignId);

        verify(userRewardRepository, never()).save(any());
    }

    @Test
    void issueReward_voucherType_logsWhenPoolEmpty() {
        UUID userId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        UUID milestoneId = UUID.randomUUID();
        UUID rewardId = UUID.randomUUID();

        Milestone m = Milestone.builder()
                .id(milestoneId)
                .stampsRequired(1)
                .name("M")
                .active(true)
                .build();
        Reward r = Reward.builder()
                .id(rewardId)
                .milestoneId(milestoneId)
                .rewardType(RewardType.VOUCHER)
                .name("R")
                .issuedCount(0)
                .active(true)
                .build();
        UserReward saved = UserReward.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .rewardId(rewardId)
                .milestoneId(milestoneId)
                .issuedAt(LocalDateTime.now(clock))
                .status(RewardStatus.ISSUED)
                .build();

        when(milestoneRepository.findById(milestoneId)).thenReturn(Optional.of(m));
        when(userRewardRepository.existsByUserIdAndMilestoneId(userId, milestoneId)).thenReturn(false);
        when(rewardRepository.findActiveByMilestoneId(milestoneId)).thenReturn(Optional.of(r));
        when(rewardRepository.incrementIssuedCountIfStockAllows(rewardId)).thenReturn(true);
        when(voucherPoolRepository.lockNextAvailableForReward(rewardId)).thenReturn(Optional.empty());
        when(userRewardRepository.save(any(UserReward.class))).thenReturn(saved);

        TransactionSynchronizationManager.initSynchronization();
        try {
            service.issueReward(userId, milestoneId, lineId);
            TransactionSynchronizationManager.getSynchronizations().forEach(s -> s.afterCommit());
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }

        verify(userRewardRepository).save(any(UserReward.class));
        verify(voucherPoolRepository, never()).save(any(VoucherPool.class));
    }
}
