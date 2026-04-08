package metro.ExoticStamp.modules.collection.infrastructure.cache;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.config.CacheProperties;
import metro.ExoticStamp.infra.cache.BaseCacheRepository;
import metro.ExoticStamp.modules.collection.application.port.UserStampCachePort;
import metro.ExoticStamp.modules.collection.application.view.ProgressView;
import metro.ExoticStamp.modules.collection.application.view.UserStampView;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
public class UserStampCacheRepository extends BaseCacheRepository<Object> implements UserStampCachePort {

    private final Duration userStampsTtl;
    private final Duration userProgressTtl;

    public UserStampCacheRepository(
            RedisTemplate<String, Object> redisTemplate,
            MeterRegistry meterRegistry,
            CacheProperties cacheProperties
    ) {
        super(redisTemplate, meterRegistry);
        this.userStampsTtl = cacheProperties.getTtl().getCollectionUserStamps();
        this.userProgressTtl = cacheProperties.getTtl().getCollectionUserProgress();
    }

    @Override
    protected String prefix() {
        return "collection:";
    }

    @Override
    protected Duration ttl() {
        return userStampsTtl;
    }

    @Override
    protected Class<Object> type() {
        return Object.class;
    }

    @Override
    public Optional<List<UserStampView>> getUserStamps(UUID userId, UUID lineId) {
        return getString(userStampsKey(userId, lineId));
    }

    @Override
    public void putUserStamps(UUID userId, UUID lineId, List<UserStampView> value) {
        putString(userStampsKey(userId, lineId), value, userStampsTtl);
    }

    @Override
    public void evictUserStamps(UUID userId, UUID lineId) {
        deleteKey(userStampsKey(userId, lineId));
    }

    @Override
    public Optional<ProgressView> getUserProgress(UUID userId, UUID lineId) {
        Object raw = getStringRaw(userProgressKey(userId, lineId));
        if (raw == null) return Optional.empty();
        try {
            return Optional.of((ProgressView) raw);
        } catch (Exception e) {
            meterRegistry.counter("cache.error", "domain", "collection").increment();
            log.warn("[UserStampCache] get progress failed key={}: {}", userProgressKey(userId, lineId), e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void putUserProgress(UUID userId, UUID lineId, ProgressView value) {
        putString(userProgressKey(userId, lineId), value, userProgressTtl);
    }

    @Override
    public void evictUserProgress(UUID userId, UUID lineId) {
        deleteKey(userProgressKey(userId, lineId));
    }

    private String userStampsKey(UUID userId, UUID lineId) {
        return "user-stamps:" + userId + ":" + lineId;
    }

    private String userProgressKey(UUID userId, UUID lineId) {
        return "user-progress:" + userId + ":" + lineId;
    }

    private Optional<List<UserStampView>> getString(String key) {
        Object raw = getStringRaw(key);
        if (raw == null) return Optional.empty();
        try {
            @SuppressWarnings("unchecked")
            List<UserStampView> casted = (List<UserStampView>) raw;
            return Optional.of(casted);
        } catch (Exception e) {
            meterRegistry.counter("cache.error", "domain", "collection").increment();
            log.warn("[UserStampCache] get failed key={}: {}", key, e.getMessage());
            return Optional.empty();
        }
    }

    private Object getStringRaw(String key) {
        try {
            Object raw = redisTemplate.opsForValue().get(key);
            if (raw == null) {
                meterRegistry.counter("cache.miss", "domain", "collection").increment();
                return null;
            }
            meterRegistry.counter("cache.hit", "domain", "collection").increment();
            return raw;
        } catch (Exception e) {
            meterRegistry.counter("cache.error", "domain", "collection").increment();
            log.warn("[UserStampCache] get failed key={}: {}", key, e.getMessage());
            return null;
        }
    }

    private void putString(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (Exception e) {
            log.warn("[UserStampCache] put failed key={}: {}", key, e.getMessage());
        }
    }

    private void deleteKey(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("[UserStampCache] delete failed key={}: {}", key, e.getMessage());
        }
    }
}

