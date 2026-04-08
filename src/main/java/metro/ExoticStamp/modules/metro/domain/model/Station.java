package metro.ExoticStamp.modules.metro.domain.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import metro.ExoticStamp.common.entity.BaseEntity;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "stations")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Station extends BaseEntity {

    @Column(name = "line_id", nullable = false)
    private UUID lineId;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Integer sequence;

    @Column(length = 500)
    private String description;

    @Column(name = "historical_info")
    private String historicalInfo;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "nfc_tag_id", unique = true, length = 100)
    private String nfcTagId;

    @Column(name = "qr_code_token", unique = true, length = 100)
    private String qrCodeToken;

    @Column(name = "collector_count", nullable = false)
    private Integer collectorCount;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

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
        if (this.code != null) {
            this.code = this.code.trim().toUpperCase();
        }
        if (this.name != null) {
            this.name = this.name.trim();
        }
        if (this.description != null) {
            this.description = this.description.trim();
        }
        if (this.historicalInfo != null) {
            this.historicalInfo = this.historicalInfo.trim();
        }
        if (this.imageUrl != null) {
            this.imageUrl = this.imageUrl.trim();
        }
        if (this.nfcTagId != null) {
            this.nfcTagId = this.nfcTagId.trim();
        }
        if (this.qrCodeToken != null) {
            this.qrCodeToken = this.qrCodeToken.trim();
        }
    }

    public void validate() {
        if (this.lineId == null) {
            throw new IllegalArgumentException("Station lineId must not be null");
        }
        if (this.code == null || this.code.isBlank()) {
            throw new IllegalArgumentException("Station code must not be blank");
        }
        if (this.code.length() > 20) {
            throw new IllegalArgumentException("Station code length must be <= 20");
        }
        if (this.name == null || this.name.isBlank()) {
            throw new IllegalArgumentException("Station name must not be blank");
        }
        if (this.name.length() > 100) {
            throw new IllegalArgumentException("Station name length must be <= 100");
        }
        if (this.sequence == null || this.sequence < 1) {
            throw new IllegalArgumentException("Station sequence must be >= 1");
        }
        if (this.description != null && this.description.length() > 500) {
            throw new IllegalArgumentException("Station description length must be <= 500");
        }
        if (this.latitude != null
                && (this.latitude.compareTo(BigDecimal.valueOf(-90)) < 0
                || this.latitude.compareTo(BigDecimal.valueOf(90)) > 0)) {
            throw new IllegalArgumentException("Station latitude must be between -90 and 90");
        }
        if (this.longitude != null
                && (this.longitude.compareTo(BigDecimal.valueOf(-180)) < 0
                || this.longitude.compareTo(BigDecimal.valueOf(180)) > 0)) {
            throw new IllegalArgumentException("Station longitude must be between -180 and 180");
        }
        if (this.nfcTagId != null && this.nfcTagId.length() > 100) {
            throw new IllegalArgumentException("Station nfcTagId length must be <= 100");
        }
        if (this.qrCodeToken != null && this.qrCodeToken.length() > 100) {
            throw new IllegalArgumentException("Station qrCodeToken length must be <= 100");
        }
        if (this.collectorCount == null || this.collectorCount < 0) {
            throw new IllegalArgumentException("Station collectorCount must be >= 0");
        }
        if (this.isActive == null) {
            throw new IllegalArgumentException("Station isActive must not be null");
        }
    }
}




