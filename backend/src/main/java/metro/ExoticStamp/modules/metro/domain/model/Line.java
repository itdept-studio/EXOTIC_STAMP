package metro.ExoticStamp.modules.metro.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import metro.ExoticStamp.common.entity.BaseEntity;
import lombok.experimental.SuperBuilder;

import java.util.regex.Pattern;

@Data
@Entity
@Table(name = "lines")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Line extends BaseEntity {

    private static final Pattern HEX_COLOR = Pattern.compile("^#[0-9A-Fa-f]{6}$");

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 7)
    private String color;

    @Column(name = "total_stations", nullable = false)
    private Integer totalStations;

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
        if (this.color != null) {
            this.color = this.color.trim();
        }
    }

    public void validate() {
        if (this.code == null || this.code.isBlank()) {
            throw new IllegalArgumentException("Line code must not be blank");
        }
        if (this.code.length() > 10) {
            throw new IllegalArgumentException("Line code length must be <= 10");
        }
        if (this.name == null || this.name.isBlank()) {
            throw new IllegalArgumentException("Line name must not be blank");
        }
        if (this.name.length() > 100) {
            throw new IllegalArgumentException("Line name length must be <= 100");
        }
        if (this.color != null && !this.color.isBlank() && !HEX_COLOR.matcher(this.color).matches()) {
            throw new IllegalArgumentException("Line color must be HEX format #RRGGBB");
        }
        if (this.totalStations == null || this.totalStations < 0) {
            throw new IllegalArgumentException("Line totalStations must be >= 0");
        }
        if (this.isActive == null) {
            throw new IllegalArgumentException("Line isActive must not be null");
        }
    }
}

