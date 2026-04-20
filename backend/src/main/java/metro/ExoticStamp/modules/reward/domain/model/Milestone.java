package metro.ExoticStamp.modules.reward.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import metro.ExoticStamp.common.entity.BaseEntity;

import java.util.UUID;

@Data
@Entity
@Table(name = "milestones")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Milestone extends BaseEntity {

    @Column(name = "line_id")
    private UUID lineId;

    @Column(name = "campaign_id")
    private UUID campaignId;

    @Column(name = "stamps_required", nullable = false)
    private Integer stampsRequired;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @PrePersist
    public void onPrePersist() {
        normalize();
        validate();
    }

    @PreUpdate
    public void onPreUpdate() {
        normalize();
        validate();
    }

    private void normalize() {
        if (name != null) {
            name = name.trim();
        }
        if (description != null) {
            description = description.trim();
        }
    }

    private void validate() {
        if (stampsRequired == null || stampsRequired < 1) {
            throw new IllegalArgumentException("Milestone stampsRequired must be >= 1");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Milestone name must not be blank");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Milestone name length must be <= 100");
        }
        if (description != null && description.length() > 255) {
            throw new IllegalArgumentException("Milestone description length must be <= 255");
        }
    }
}
