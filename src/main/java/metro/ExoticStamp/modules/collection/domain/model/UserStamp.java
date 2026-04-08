package metro.ExoticStamp.modules.collection.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import metro.ExoticStamp.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "user_stamps")
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class UserStamp extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "station_id", nullable = false)
    private UUID stationId;

    @Column(name = "stamp_design_id", nullable = false)
    private UUID stampDesignId;

    @Column(name = "campaign_id")
    private UUID campaignId;

    @Column(name = "collected_at", nullable = false)
    private LocalDateTime collectedAt;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "gps_verified", nullable = false)
    private boolean gpsVerified;

    @Enumerated(EnumType.STRING)
    @Column(name = "collect_method", nullable = false, length = 10)
    private CollectMethod collectMethod;

    @Column(name = "device_fingerprint", nullable = false, length = 255)
    private String deviceFingerprint;

    @Column(name = "idempotency_key", nullable = false, length = 36, unique = true)
    private String idempotencyKey;
}

