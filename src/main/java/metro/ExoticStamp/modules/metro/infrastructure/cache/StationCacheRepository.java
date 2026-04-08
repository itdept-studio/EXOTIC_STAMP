package metro.ExoticStamp.modules.metro.infrastructure.cache;

import java.util.UUID;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.config.CacheProperties;
import metro.ExoticStamp.infra.cache.BaseCacheRepository;
import metro.ExoticStamp.modules.metro.application.port.StationCachePort;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationDetailResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

/**
 * Station hot-path and detail cache. Keys: {@code station:nfc:*}, {@code station:qr:*}, {@code station:detail:*}.
 * Extends {@link BaseCacheRepository} for shared metrics/TTL wiring; string-key lookups use explicit key builders.
 */
@Slf4j
@Repository
public class StationCacheRepository extends BaseCacheRepository<StationDetailResponse> implements StationCachePort {

    private static final String NFC_SUFFIX = "nfc:";
    private static final String QR_SUFFIX = "qr:";
    private static final String DETAIL_SUFFIX = "detail:";

    private final Duration scanTtl;
    private final Duration detailTtl;

    public StationCacheRepository(
            RedisTemplate<String, Object> redisTemplate,
            MeterRegistry meterRegistry,
            CacheProperties cacheProperties
    ) {
        super(redisTemplate, meterRegistry);
        this.scanTtl = cacheProperties.getTtl().getMetroStationScan();
        this.detailTtl = cacheProperties.getTtl().getStationDetail();
    }

    @Override
    protected String prefix() {
        return "station:";
    }

    @Override
    protected Duration ttl() {
        return scanTtl;
    }

    @Override
    protected Class<StationDetailResponse> type() {
        return StationDetailResponse.class;
    }

    @Override
    public Optional<StationDetailResponse> getByNfcTagId(String nfcTagId) {
        return getString(prefix() + NFC_SUFFIX + nfcTagId, scanTtl);
    }

    @Override
    public Optional<StationDetailResponse> getByQrToken(String qrToken) {
        return getString(prefix() + QR_SUFFIX + qrToken, scanTtl);
    }

    @Override
    public Optional<StationDetailResponse> getByStationId(UUID stationId) {
        return getString(prefix() + DETAIL_SUFFIX + stationId, detailTtl);
    }

    @Override
    public void putByNfcTagId(String nfcTagId, StationDetailResponse value) {
        putString(prefix() + NFC_SUFFIX + nfcTagId, value, scanTtl);
    }

    @Override
    public void putByQrToken(String qrToken, StationDetailResponse value) {
        putString(prefix() + QR_SUFFIX + qrToken, value, scanTtl);
    }

    @Override
    public void putByStationId(UUID stationId, StationDetailResponse value) {
        putString(prefix() + DETAIL_SUFFIX + stationId, value, detailTtl);
    }

    @Override
    public void evictByNfcTagId(String nfcTagId) {
        if (nfcTagId != null && !nfcTagId.isBlank()) {
            deleteKey(prefix() + NFC_SUFFIX + nfcTagId);
        }
    }

    @Override
    public void evictByQrToken(String qrToken) {
        if (qrToken != null && !qrToken.isBlank()) {
            deleteKey(prefix() + QR_SUFFIX + qrToken);
        }
    }

    @Override
    public void evictDetailByStationId(UUID stationId) {
        if (stationId != null) {
            deleteKey(prefix() + DETAIL_SUFFIX + stationId);
        }
    }

    private Optional<StationDetailResponse> getString(String key, Duration ttl) {
        try {
            Object raw = redisTemplate.opsForValue().get(key);
            if (raw == null) {
                meterRegistry.counter("cache.miss", "domain", "station").increment();
                return Optional.empty();
            }
            meterRegistry.counter("cache.hit", "domain", "station").increment();
            return Optional.of(type().cast(raw));
        } catch (Exception e) {
            meterRegistry.counter("cache.error", "domain", "station").increment();
            log.warn("[StationCache] get failed key={}: {}", key, e.getMessage());
            return Optional.empty();
        }
    }

    private void putString(String key, StationDetailResponse value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (Exception e) {
            log.warn("[StationCache] put failed key={}: {}", key, e.getMessage());
        }
    }

    private void deleteKey(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("[StationCache] delete failed key={}: {}", key, e.getMessage());
        }
    }
}



