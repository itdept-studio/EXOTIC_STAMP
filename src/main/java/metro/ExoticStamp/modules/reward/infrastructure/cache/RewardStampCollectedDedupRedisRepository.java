package metro.ExoticStamp.modules.reward.infrastructure.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.modules.reward.application.port.RewardStampCollectedDedupPort;
import metro.ExoticStamp.modules.reward.config.RewardProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RewardStampCollectedDedupRedisRepository implements RewardStampCollectedDedupPort {

    private static final String PREFIX = "reward:dedup:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final RewardProperties rewardProperties;

    @Override
    public boolean claimFirstProcessing(UUID eventId) {
        try {
            String key = PREFIX + eventId;
            Duration ttl = rewardProperties.getStampCollectedEventDedupTtl();
            Boolean first = redisTemplate.opsForValue().setIfAbsent(key, "1", ttl);
            return Boolean.TRUE.equals(first);
        } catch (Exception e) {
            log.warn("[Reward] stamp collected dedup unavailable eventId={}: {}", eventId, e.getMessage());
            return true;
        }
    }
}
