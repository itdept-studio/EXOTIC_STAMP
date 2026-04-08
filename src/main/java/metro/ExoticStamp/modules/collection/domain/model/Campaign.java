package metro.ExoticStamp.modules.collection.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import metro.ExoticStamp.common.entity.BaseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "campaigns")
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Campaign extends BaseEntity {

    @Column(name = "partner_id")
    private UUID partnerId;

    /**
     * Metro line id (UUID) stored as a scalar to avoid cross-module JPA relationships.
     */
    @Column(name = "line_id")
    private UUID lineId;

    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "banner_url", length = 255)
    private String bannerUrl;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;
}

