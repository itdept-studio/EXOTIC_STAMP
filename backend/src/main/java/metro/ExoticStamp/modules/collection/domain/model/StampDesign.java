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

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "stamp_designs")
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class StampDesign extends BaseEntity {

    @Column(name = "station_id")
    private UUID stationId;

    @Column(name = "campaign_id")
    private UUID campaignId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "artwork_url", nullable = false, length = 255)
    private String artworkUrl;

    @Column(name = "animation_url", length = 255)
    private String animationUrl;

    @Column(name = "sound_url", length = 255)
    private String soundUrl;

    @Column(name = "is_limited", nullable = false)
    private boolean isLimited;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;
}

