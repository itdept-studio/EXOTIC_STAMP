package metro.ExoticStamp.modules.reward.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Maps voucher_pool only (no BaseEntity — table has no audit columns).
 */
@Data
@Entity
@Table(name = "voucher_pool")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherPool {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "reward_id", nullable = false)
    private UUID rewardId;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(name = "is_redeemed", nullable = false)
    private boolean redeemed;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onPrePersist() {
        normalize();
        validate();
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void onPreUpdate() {
        normalize();
        validate();
    }

    private void normalize() {
        if (code != null) {
            code = code.trim();
        }
    }

    private void validate() {
        if (rewardId == null) {
            throw new IllegalArgumentException("VoucherPool rewardId must not be null");
        }
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("VoucherPool code must not be blank");
        }
        if (code.length() > 50) {
            throw new IllegalArgumentException("VoucherPool code length must be <= 50");
        }
    }
}
