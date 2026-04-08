# METRO MODULE - STEP-BY-STEP IMPLEMENTATION GUIDE

> Hướng dẫn chi tiết implement Metro module, từ domain đến presentation layer.
> Ưu tiên: Balanced (MVP nhanh + DDD quality + performance)
> Monetization: Full (Ads + Affiliate + Partners)

---

## PHASE 0: SETUP & PLANNING

### ✅ Database Schema (Already Ready)

Schema từ `V2__metro.sql` - bạn đã có:

```sql
CREATE TABLE lines (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    color VARCHAR(7),  -- hex color
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE stations (
    id UUID PRIMARY KEY,
    line_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    nfc_tag_id VARCHAR(100) UNIQUE NOT NULL,        -- ⭐ HOT-PATH
    qr_code_token VARCHAR(255) UNIQUE NOT NULL,    -- ⭐ HOT-PATH
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_station_line FOREIGN KEY (line_id) REFERENCES lines(id),
    INDEX idx_nfc_tag_id (nfc_tag_id),              -- ⭐ IMPORTANT
    INDEX idx_qr_code_token (qr_code_token),       -- ⭐ IMPORTANT
    INDEX idx_line_id (line_id)
);
```

### 📊 File Structure (Create these)

```
src/main/java/metro/ExoticStamp/modules/metro/
├── domain/
│   ├── entity/
│   │   ├── LineEntity.java          (JPA entity)
│   │   ├── StationEntity.java       (JPA entity)
│   │   └── StationStatus.java       (enum)
│   ├── model/
│   │   ├── Line.java                (domain model)
│   │   └── Station.java             (domain model)
│   ├── repository/
│   │   ├── LineRepository.java      (interface - NO IMPL)
│   │   └── StationRepository.java   (interface - NO IMPL)
│   ├── service/
│   │   └── MetroDomainService.java  (business rules)
│   └── exception/
│       ├── LineNotFoundException.java
│       ├── StationNotFoundException.java
│       └── StationInactiveException.java
│
├── application/
│   ├── service/
│   │   └── StationQueryService.java (readOnly=true)
│   ├── mapper/
│   │   ├── LineAppMapper.java
│   │   └── StationAppMapper.java
│   └── dto/
│       ├── command/
│       └── query/
│
├── infrastructure/
│   ├── repository/
│   │   ├── JpaLineRepository.java
│   │   ├── JpaStationRepository.java
│   │   ├── LineRepositoryAdapter.java
│   │   └── StationRepositoryAdapter.java
│   ├── cache/
│   │   ├── LineCacheRepository.java
│   │   └── StationCacheRepository.java
│   └── persistence/
│
└── presentation/
    ├── controller/
    │   └── MetroController.java
    ├── request/
    ├── response/
    │   ├── LineResponse.java
    │   ├── StationResponse.java
    │   └── StationListResponse.java
    └── exception/
```

---

## PHASE 1: DOMAIN LAYER

### Step 1.1: Create Line Domain Model

**File**: `modules/metro/domain/model/Line.java`

```java
package metro.ExoticStamp.modules.metro.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain model for Line (immutable aggregate).
 * No JPA annotations - this is pure domain.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Line {
    
    private UUID id;
    private String name;
    private String color;  // hex color, e.g., "#FF0000"
    private String description;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Static factory
    public static Line create(String name, String color, String description) {
        return Line.builder()
            .id(UUID.randomUUID())
            .name(name)
            .color(color)
            .description(description)
            .active(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
    
    // Domain method
    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }
}
```

### Step 1.2: Create Station Domain Model

**File**: `modules/metro/domain/model/Station.java`

```java
package metro.ExoticStamp.modules.metro.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import metro.ExoticStamp.modules.metro.domain.exception.StationInactiveException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain model for Station (immutable aggregate).
 * Contains business rules validation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Station {
    
    private UUID id;
    private UUID lineId;
    private String name;
    private String nfcTagId;
    private String qrCodeToken;
    private Double latitude;
    private Double longitude;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Static factory
    public static Station create(
        UUID lineId,
        String name,
        String nfcTagId,
        String qrCodeToken,
        Double latitude,
        Double longitude
    ) {
        return Station.builder()
            .id(UUID.randomUUID())
            .lineId(lineId)
            .name(name)
            .nfcTagId(nfcTagId)
            .qrCodeToken(qrCodeToken)
            .latitude(latitude)
            .longitude(longitude)
            .active(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
    
    // Domain validation methods
    public void validateActive() {
        if (!this.active) {
            throw new StationInactiveException(this.id);
        }
    }
    
    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Check if this station is within GPS radius (for anti-cheat).
     * Returns true if user's GPS is within ~50m of station.
     */
    public boolean isWithinGpsRadius(Double userLat, Double userLng, Double radiusMeters) {
        if (userLat == null || userLng == null || radiusMeters == null) {
            return true;  // skip check if no GPS provided
        }
        
        double distance = calculateDistance(this.latitude, this.longitude, userLat, userLng);
        return distance <= radiusMeters;
    }
    
    private double calculateDistance(Double lat1, Double lng1, Double lat2, Double lng2) {
        // Haversine formula (simplified)
        double earthRadiusKm = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c * 1000;  // convert to meters
    }
}
```

### Step 1.3: Create Domain Exceptions

**File**: `modules/metro/domain/exception/LineNotFoundException.java`

```java
package metro.ExoticStamp.modules.metro.domain.exception;

import metro.ExoticStamp.common.exceptions.DomainException;
import org.springframework.http.HttpStatus;
import java.util.UUID;

public class LineNotFoundException extends DomainException {
    
    public LineNotFoundException(UUID lineId) {
        super("Line not found with id: " + lineId);
        this.errorCode = "LINE_NOT_FOUND";
        this.httpStatus = HttpStatus.NOT_FOUND;
    }
    
    public LineNotFoundException(String name) {
        super("Line not found with name: " + name);
        this.errorCode = "LINE_NOT_FOUND";
        this.httpStatus = HttpStatus.NOT_FOUND;
    }
}
```

**File**: `modules/metro/domain/exception/StationNotFoundException.java`

```java
package metro.ExoticStamp.modules.metro.domain.exception;

import metro.ExoticStamp.common.exceptions.DomainException;
import org.springframework.http.HttpStatus;
import java.util.UUID;

public class StationNotFoundException extends DomainException {
    
    public StationNotFoundException(UUID stationId) {
        super("Station not found with id: " + stationId);
        this.errorCode = "STATION_NOT_FOUND";
        this.httpStatus = HttpStatus.NOT_FOUND;
    }
    
    public StationNotFoundException(String nfcTag, String searchType) {
        super("Station not found with " + searchType + ": " + nfcTag);
        this.errorCode = "STATION_NOT_FOUND";
        this.httpStatus = HttpStatus.NOT_FOUND;
    }
}
```

**File**: `modules/metro/domain/exception/StationInactiveException.java`

```java
package metro.ExoticStamp.modules.metro.domain.exception;

import metro.ExoticStamp.common.exceptions.DomainException;
import org.springframework.http.HttpStatus;
import java.util.UUID;

public class StationInactiveException extends DomainException {
    
    public StationInactiveException(UUID stationId) {
        super("Station is inactive: " + stationId);
        this.errorCode = "STATION_INACTIVE";
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }
}
```

### Step 1.4: Create Domain Repository Interfaces

**File**: `modules/metro/domain/repository/LineRepository.java`

```java
package metro.ExoticStamp.modules.metro.domain.repository;

import metro.ExoticStamp.modules.metro.domain.model.Line;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ❌ NO IMPLEMENTATION HERE
 * This is interface only. Implementation in infrastructure layer.
 */
public interface LineRepository {
    
    Optional<Line> findById(UUID id);
    
    Optional<Line> findByName(String name);
    
    List<Line> findAll();
    
    List<Line> findAllActive();
    
    Line save(Line line);
    
    void delete(UUID id);
}
```

**File**: `modules/metro/domain/repository/StationRepository.java`

```java
package metro.ExoticStamp.modules.metro.domain.repository;

import metro.ExoticStamp.modules.metro.domain.model.Station;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ❌ NO IMPLEMENTATION HERE
 * This is interface only. Implementation in infrastructure layer.
 */
public interface StationRepository {
    
    Optional<Station> findById(UUID id);
    
    // ⭐ HOT-PATH: NFC scan
    Optional<Station> findByNfcTagId(String nfcTagId);
    
    // ⭐ HOT-PATH: QR scan
    Optional<Station> findByQrCodeToken(String qrCodeToken);
    
    List<Station> findByLineId(UUID lineId);
    
    List<Station> findByLineIdAndActive(UUID lineId, boolean active);
    
    List<Station> findAllActive();
    
    Station save(Station station);
    
    void delete(UUID id);
}
```

### Step 1.5: Create Domain Service

**File**: `modules/metro/domain/service/MetroDomainService.java`

```java
package metro.ExoticStamp.modules.metro.domain.service;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.metro.domain.exception.StationInactiveException;
import metro.ExoticStamp.modules.metro.domain.model.Station;
import metro.ExoticStamp.modules.metro.domain.repository.LineRepository;
import metro.ExoticStamp.modules.metro.domain.repository.StationRepository;
import org.springframework.stereotype.Service;

/**
 * Domain service for Metro business rules validation.
 */
@Service
@RequiredArgsConstructor
public class MetroDomainService {
    
    private final StationRepository stationRepository;
    private final LineRepository lineRepository;
    
    /**
     * Validate station is active and can be scanned.
     * 
     * @throws StationInactiveException if station inactive
     */
    public void validateStationForScan(Station station) {
        station.validateActive();  // throws if inactive
    }
    
    /**
     * Validate station exists and is within GPS radius (anti-cheat).
     * 
     * @param station the station to validate
     * @param userLat user's GPS latitude (nullable)
     * @param userLng user's GPS longitude (nullable)
     * @param radiusMeters GPS accuracy threshold (nullable)
     * @return true if within radius (or GPS check disabled)
     */
    public boolean validateGpsProximity(
        Station station,
        Double userLat,
        Double userLng,
        Double radiusMeters
    ) {
        return station.isWithinGpsRadius(userLat, userLng, radiusMeters);
    }
}
```

---

## PHASE 2: INFRASTRUCTURE LAYER

### Step 2.1: Create JPA Entities

**File**: `modules/metro/infrastructure/persistence/LineEntity.java`

```java
package metro.ExoticStamp.modules.metro.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import metro.ExoticStamp.common.entity.BaseEntity;
import java.time.LocalDateTime;

@Entity
@Table(name = "lines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LineEntity extends BaseEntity {
    
    @Column(nullable = false, unique = true, length = 255)
    private String name;
    
    @Column(length = 7)  // hex color: #RRGGBB
    private String color;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private boolean isActive = true;
}
```

**File**: `modules/metro/infrastructure/persistence/StationEntity.java`

```java
package metro.ExoticStamp.modules.metro.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import metro.ExoticStamp.common.entity.BaseEntity;
import java.util.UUID;

@Entity
@Table(
    name = "stations",
    indexes = {
        @Index(name = "idx_nfc_tag_id", columnList = "nfc_tag_id"),        // ⭐ HOT-PATH
        @Index(name = "idx_qr_code_token", columnList = "qr_code_token"),  // ⭐ HOT-PATH
        @Index(name = "idx_line_id", columnList = "line_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StationEntity extends BaseEntity {
    
    @Column(nullable = false)
    private UUID lineId;  // ❌ NO foreign key - reference via UUID only
    
    @Column(nullable = false, length = 255)
    private String name;
    
    @Column(nullable = false, unique = true, length = 100)
    private String nfcTagId;
    
    @Column(nullable = false, unique = true, length = 255)
    private String qrCodeToken;
    
    @Column(nullable = false, precision = 10, scale = 8)
    private Double latitude;
    
    @Column(nullable = false, precision = 11, scale = 8)
    private Double longitude;
    
    @Column(nullable = false)
    private boolean isActive = true;
}
```

### Step 2.2: Create JPA Repositories

**File**: `modules/metro/infrastructure/persistence/JpaLineRepository.java`

```java
package metro.ExoticStamp.infrastructure.repository;

import metro.ExoticStamp.modules.metro.infrastructure.persistence.LineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaLineRepository extends JpaRepository<LineEntity, UUID> {
    
    Optional<LineEntity> findByName(String name);
    
    @Query("SELECT l FROM LineEntity l WHERE l.isActive = true ORDER BY l.name ASC")
    List<LineEntity> findAllActive();
}
```

**File**: `modules/metro/infrastructure/persistence/JpaStationRepository.java`

```java
package metro.ExoticStamp.infrastructure.repository;

import metro.ExoticStamp.modules.metro.infrastructure.persistence.StationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaStationRepository extends JpaRepository<StationEntity, UUID> {
    
    // ⭐ HOT-PATH: indexed queries
    Optional<StationEntity> findByNfcTagId(String nfcTagId);
    
    Optional<StationEntity> findByQrCodeToken(String qrCodeToken);
    
    // Standard queries
    List<StationEntity> findByLineId(UUID lineId);
    
    @Query("SELECT s FROM StationEntity s WHERE s.lineId = :lineId AND s.isActive = true")
    List<StationEntity> findByLineIdAndActive(UUID lineId, boolean active);
    
    @Query("SELECT s FROM StationEntity s WHERE s.isActive = true ORDER BY s.lineId, s.name")
    List<StationEntity> findAllActive();
}
```

### Step 2.3: Create Repository Adapters

**File**: `modules/metro/infrastructure/repository/LineRepositoryAdapter.java`

```java
package metro.ExoticStamp.modules.metro.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.metro.domain.model.Line;
import metro.ExoticStamp.modules.metro.domain.repository.LineRepository;
import metro.ExoticStamp.modules.metro.infrastructure.persistence.JpaLineRepository;
import metro.ExoticStamp.modules.metro.infrastructure.persistence.LineEntity;
import metro.ExoticStamp.modules.metro.application.mapper.LineAppMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter that bridges domain LineRepository interface
 * to JPA infrastructure layer.
 */
@Component
@RequiredArgsConstructor
public class LineRepositoryAdapter implements LineRepository {
    
    private final JpaLineRepository jpaRepository;
    private final LineAppMapper mapper;  // entity ↔ domain mapping
    
    @Override
    public Optional<Line> findById(UUID id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }
    
    @Override
    public Optional<Line> findByName(String name) {
        return jpaRepository.findByName(name)
            .map(mapper::toDomain);
    }
    
    @Override
    public List<Line> findAll() {
        return jpaRepository.findAll()
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Line> findAllActive() {
        return jpaRepository.findAllActive()
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public Line save(Line line) {
        LineEntity entity = mapper.toEntity(line);
        LineEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }
}
```

**File**: `modules/metro/infrastructure/repository/StationRepositoryAdapter.java`

```java
package metro.ExoticStamp.modules.metro.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.metro.domain.model.Station;
import metro.ExoticStamp.modules.metro.domain.repository.StationRepository;
import metro.ExoticStamp.modules.metro.infrastructure.persistence.JpaStationRepository;
import metro.ExoticStamp.modules.metro.infrastructure.persistence.StationEntity;
import metro.ExoticStamp.modules.metro.application.mapper.StationAppMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter that bridges domain StationRepository interface
 * to JPA infrastructure layer.
 * 
 * Hot-path queries (NFC/QR) benefit from DB indexes.
 */
@Component
@RequiredArgsConstructor
public class StationRepositoryAdapter implements StationRepository {
    
    private final JpaStationRepository jpaRepository;
    private final StationAppMapper mapper;
    
    @Override
    public Optional<Station> findById(UUID id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }
    
    @Override
    public Optional<Station> findByNfcTagId(String nfcTagId) {
        return jpaRepository.findByNfcTagId(nfcTagId)
            .map(mapper::toDomain);
    }
    
    @Override
    public Optional<Station> findByQrCodeToken(String qrCodeToken) {
        return jpaRepository.findByQrCodeToken(qrCodeToken)
            .map(mapper::toDomain);
    }
    
    @Override
    public List<Station> findByLineId(UUID lineId) {
        return jpaRepository.findByLineId(lineId)
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Station> findByLineIdAndActive(UUID lineId, boolean active) {
        return jpaRepository.findByLineIdAndActive(lineId, active)
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Station> findAllActive() {
        return jpaRepository.findAllActive()
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public Station save(Station station) {
        StationEntity entity = mapper.toEntity(station);
        StationEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }
}
```

### Step 2.4: Create Cache Repositories

**File**: `modules/metro/infrastructure/cache/LineCacheRepository.java`

```java
package metro.ExoticStamp.modules.metro.infrastructure.cache;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.metro.presentation.response.LineResponse;
import metro.ExoticStamp.infra.cache.BaseCacheRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LineCacheRepository extends BaseCacheRepository<Object> {
    
    private static final String CACHE_PREFIX = "line:";
    private static final String ALL_LINES_KEY = "all-lines";
    private static final String ACTIVE_LINES_KEY = "active-lines";
    
    @Value("${cache.ttl.metro-lines:1800}")  // 30 minutes default
    private long ttl;
    
    public List<LineResponse> getAllLines() {
        return super.get(CACHE_PREFIX + ALL_LINES_KEY, List.class);
    }
    
    public void setAllLines(List<LineResponse> lines) {
        super.set(CACHE_PREFIX + ALL_LINES_KEY, lines, Duration.ofSeconds(ttl));
    }
    
    public List<LineResponse> getActiveLines() {
        return super.get(CACHE_PREFIX + ACTIVE_LINES_KEY, List.class);
    }
    
    public void setActiveLines(List<LineResponse> lines) {
        super.set(CACHE_PREFIX + ACTIVE_LINES_KEY, lines, Duration.ofSeconds(ttl));
    }
    
    public void evictAll() {
        super.delete(CACHE_PREFIX + ALL_LINES_KEY);
        super.delete(CACHE_PREFIX + ACTIVE_LINES_KEY);
    }
}
```

**File**: `modules/metro/infrastructure/cache/StationCacheRepository.java`

```java
package metro.ExoticStamp.modules.metro.infrastructure.cache;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.metro.presentation.response.StationListResponse;
import metro.ExoticStamp.infra.cache.BaseCacheRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StationCacheRepository extends BaseCacheRepository<Object> {
    
    private static final String CACHE_PREFIX = "station:";
    
    @Value("${cache.ttl.metro-stations:1800}")  // 30 minutes default
    private long ttl;
    
    public StationListResponse getByLineId(UUID lineId) {
        return super.get(CACHE_PREFIX + "line:" + lineId, StationListResponse.class);
    }
    
    public void setByLineId(UUID lineId, StationListResponse response) {
        super.set(CACHE_PREFIX + "line:" + lineId, response, Duration.ofSeconds(ttl));
    }
    
    public void evictByLineId(UUID lineId) {
        super.delete(CACHE_PREFIX + "line:" + lineId);
    }
    
    public void evictAll() {
        // ⚠️ Redis doesn't have pattern delete, implement carefully if needed
        // For now, manual eviction on writes
    }
}
```

---

## PHASE 3: APPLICATION LAYER

### Step 3.1: Create Application Mapper

**File**: `modules/metro/application/mapper/LineAppMapper.java`

```java
package metro.ExoticStamp.modules.metro.application.mapper;

import metro.ExoticStamp.modules.metro.domain.model.Line;
import metro.ExoticStamp.modules.metro.infrastructure.persistence.LineEntity;
import metro.ExoticStamp.modules.metro.presentation.response.LineResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LineAppMapper {
    
    // Domain ↔ Response DTO
    public LineResponse toResponse(Line line) {
        if (line == null) return null;
        
        return LineResponse.builder()
            .id(line.getId())
            .name(line.getName())
            .color(line.getColor())
            .description(line.getDescription())
            .isActive(line.isActive())
            .createdAt(line.getCreatedAt())
            .build();
    }
    
    public List<LineResponse> toResponseList(List<Line> lines) {
        return lines.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    // JPA Entity ↔ Domain (for adapter)
    public Line toDomain(LineEntity entity) {
        if (entity == null) return null;
        
        return Line.builder()
            .id(entity.getId())
            .name(entity.getName())
            .color(entity.getColor())
            .description(entity.getDescription())
            .active(entity.isActive())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
    
    public LineEntity toEntity(Line line) {
        if (line == null) return null;
        
        return LineEntity.builder()
            .id(line.getId())
            .name(line.getName())
            .color(line.getColor())
            .description(line.getDescription())
            .isActive(line.isActive())
            .build();
    }
}
```

**File**: `modules/metro/application/mapper/StationAppMapper.java`

```java
package metro.ExoticStamp.modules.metro.application.mapper;

import metro.ExoticStamp.modules.metro.domain.model.Station;
import metro.ExoticStamp.modules.metro.infrastructure.persistence.StationEntity;
import metro.ExoticStamp.modules.metro.presentation.response.StationResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class StationAppMapper {
    
    // Domain ↔ Response DTO
    public StationResponse toResponse(Station station) {
        if (station == null) return null;
        
        return StationResponse.builder()
            .id(station.getId())
            .lineId(station.getLineId())
            .name(station.getName())
            .nfcTagId(station.getNfcTagId())  // expose for admin only (in controller filter)
            .qrCodeToken(station.getQrCodeToken())  // expose for admin only
            .latitude(station.getLatitude())
            .longitude(station.getLongitude())
            .isActive(station.isActive())
            .createdAt(station.getCreatedAt())
            .build();
    }
    
    public List<StationResponse> toResponseList(List<Station> stations) {
        return stations.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    // JPA Entity ↔ Domain (for adapter)
    public Station toDomain(StationEntity entity) {
        if (entity == null) return null;
        
        return Station.builder()
            .id(entity.getId())
            .lineId(entity.getLineId())
            .name(entity.getName())
            .nfcTagId(entity.getNfcTagId())
            .qrCodeToken(entity.getQrCodeToken())
            .latitude(entity.getLatitude())
            .longitude(entity.getLongitude())
            .active(entity.isActive())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
    
    public StationEntity toEntity(Station station) {
        if (station == null) return null;
        
        return StationEntity.builder()
            .id(station.getId())
            .lineId(station.getLineId())
            .name(station.getName())
            .nfcTagId(station.getNfcTagId())
            .qrCodeToken(station.getQrCodeToken())
            .latitude(station.getLatitude())
            .longitude(station.getLongitude())
            .isActive(station.isActive())
            .build();
    }
}
```

### Step 3.2: Create Query Service

**File**: `modules/metro/application/service/StationQueryService.java`

```java
package metro.ExoticStamp.modules.metro.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.modules.metro.domain.model.Line;
import metro.ExoticStamp.modules.metro.domain.model.Station;
import metro.ExoticStamp.modules.metro.domain.repository.LineRepository;
import metro.ExoticStamp.modules.metro.domain.repository.StationRepository;
import metro.ExoticStamp.modules.metro.application.mapper.LineAppMapper;
import metro.ExoticStamp.modules.metro.application.mapper.StationAppMapper;
import metro.ExoticStamp.modules.metro.infrastructure.cache.LineCacheRepository;
import metro.ExoticStamp.modules.metro.infrastructure.cache.StationCacheRepository;
import metro.ExoticStamp.modules.metro.presentation.response.LineResponse;
import metro.ExoticStamp.modules.metro.presentation.response.StationListResponse;
import metro.ExoticStamp.modules.metro.presentation.response.StationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Query service for Metro module.
 * All methods readOnly=true (no side effects).
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StationQueryService {
    
    private final LineRepository lineRepository;
    private final StationRepository stationRepository;
    private final LineAppMapper lineMapper;
    private final StationAppMapper stationMapper;
    private final LineCacheRepository lineCacheRepository;
    private final StationCacheRepository stationCacheRepository;
    
    // ==================== LINE QUERIES ====================
    
    public List<LineResponse> getAllLines() {
        log.debug("Fetching all lines");
        
        // Try cache first
        List<LineResponse> cached = lineCacheRepository.getAllLines();
        if (cached != null && !cached.isEmpty()) {
            log.debug("Cache hit: all lines");
            return cached;
        }
        
        // Cache miss: query from DB
        List<Line> lines = lineRepository.findAll();
        List<LineResponse> responses = lineMapper.toResponseList(lines);
        
        // Store in cache
        lineCacheRepository.setAllLines(responses);
        log.debug("Cached {} lines", responses.size());
        
        return responses;
    }
    
    public List<LineResponse> getActiveLines() {
        log.debug("Fetching active lines");
        
        // Try cache first
        List<LineResponse> cached = lineCacheRepository.getActiveLines();
        if (cached != null && !cached.isEmpty()) {
            log.debug("Cache hit: active lines");
            return cached;
        }
        
        // Cache miss: query from DB
        List<Line> lines = lineRepository.findAllActive();
        List<LineResponse> responses = lineMapper.toResponseList(lines);
        
        // Store in cache
        lineCacheRepository.setActiveLines(responses);
        log.debug("Cached {} active lines", responses.size());
        
        return responses;
    }
    
    public LineResponse getLineById(UUID lineId) {
        log.debug("Fetching line by id: {}", lineId);
        
        Line line = lineRepository.findById(lineId)
            .orElseThrow(() -> new metro.ExoticStamp.modules.metro.domain.exception.LineNotFoundException(lineId));
        
        return lineMapper.toResponse(line);
    }
    
    // ==================== STATION QUERIES ====================
    
    public StationListResponse getStationsByLine(UUID lineId) {
        log.debug("Fetching stations by line: {}", lineId);
        
        // Try cache first
        StationListResponse cached = stationCacheRepository.getByLineId(lineId);
        if (cached != null && !cached.getStations().isEmpty()) {
            log.debug("Cache hit: stations for line {}", lineId);
            return cached;
        }
        
        // Verify line exists
        lineRepository.findById(lineId)
            .orElseThrow(() -> new metro.ExoticStamp.modules.metro.domain.exception.LineNotFoundException(lineId));
        
        // Query stations
        List<Station> stations = stationRepository.findByLineIdAndActive(lineId, true);
        List<StationResponse> responses = stationMapper.toResponseList(stations);
        
        // Build response
        StationListResponse response = StationListResponse.builder()
            .lineId(lineId)
            .totalCount(responses.size())
            .stations(responses)
            .build();
        
        // Cache result
        stationCacheRepository.setByLineId(lineId, response);
        log.debug("Cached {} stations for line {}", responses.size(), lineId);
        
        return response;
    }
    
    // ==================== HOT-PATH QUERIES (NFC/QR) ====================
    
    /**
     * ⭐ HOT-PATH: Resolve station by NFC tag.
     * This is called frequently during app scan operations.
     * 
     * Database has index on nfc_tag_id for fast lookup.
     */
    public StationResponse resolveStationByNfc(String nfcTagId) {
        log.debug("Resolving station by NFC: {}", nfcTagId);
        
        Station station = stationRepository.findByNfcTagId(nfcTagId)
            .orElseThrow(() -> new metro.ExoticStamp.modules.metro.domain.exception.StationNotFoundException(nfcTagId, "NFC"));
        
        // Validate station is active
        station.validateActive();
        
        return stationMapper.toResponse(station);
    }
    
    /**
     * ⭐ HOT-PATH: Resolve station by QR code.
     * This is called frequently during app scan operations.
     * 
     * Database has index on qr_code_token for fast lookup.
     */
    public StationResponse resolveStationByQr(String qrCodeToken) {
        log.debug("Resolving station by QR: {}", qrCodeToken);
        
        Station station = stationRepository.findByQrCodeToken(qrCodeToken)
            .orElseThrow(() -> new metro.ExoticStamp.modules.metro.domain.exception.StationNotFoundException(qrCodeToken, "QR"));
        
        // Validate station is active
        station.validateActive();
        
        return stationMapper.toResponse(station);
    }
}
```

---

## PHASE 4: PRESENTATION LAYER

### Step 4.1: Create Response DTOs

**File**: `modules/metro/presentation/response/LineResponse.java`

```java
package metro.ExoticStamp.modules.metro.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Line response DTO")
public class LineResponse {
    
    @Schema(example = "550e8400-e29b-41d4-a716-446655440000", description = "Line ID")
    private UUID id;
    
    @Schema(example = "Line 1 - Red", description = "Line name")
    private String name;
    
    @Schema(example = "#FF0000", description = "Line color in hex format")
    private String color;
    
    @Schema(example = "Downtown to Airport", description = "Line description")
    private String description;
    
    @Schema(example = "true", description = "Is line active")
    private boolean isActive;
    
    @Schema(example = "2024-01-15T10:30:00", description = "Created timestamp")
    private LocalDateTime createdAt;
}
```

**File**: `modules/metro/presentation/response/StationResponse.java`

```java
package metro.ExoticStamp.modules.metro.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Station response DTO")
public class StationResponse {
    
    @Schema(example = "550e8400-e29b-41d4-a716-446655440001", description = "Station ID")
    private UUID id;
    
    @Schema(example = "550e8400-e29b-41d4-a716-446655440000", description = "Line ID")
    private UUID lineId;
    
    @Schema(example = "Central Station", description = "Station name")
    private String name;
    
    @Schema(example = "NFC_TAG_001", description = "NFC tag identifier (admin only)")
    private String nfcTagId;
    
    @Schema(example = "QR_TOKEN_ABC123", description = "QR code token (admin only)")
    private String qrCodeToken;
    
    @Schema(example = "10.77225410", description = "Latitude coordinate")
    private Double latitude;
    
    @Schema(example = "106.69880027", description = "Longitude coordinate")
    private Double longitude;
    
    @Schema(example = "true", description = "Is station active")
    private boolean isActive;
    
    @Schema(example = "2024-01-15T10:30:00", description = "Created timestamp")
    private LocalDateTime createdAt;
}
```

**File**: `modules/metro/presentation/response/StationListResponse.java`

```java
package metro.ExoticStamp.modules.metro.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Paginated station list response")
public class StationListResponse {
    
    @Schema(example = "550e8400-e29b-41d4-a716-446655440000", description = "Line ID")
    private UUID lineId;
    
    @Schema(example = "15", description = "Total count of stations")
    private int totalCount;
    
    @Schema(description = "List of stations")
    private List<StationResponse> stations;
}
```

### Step 4.2: Create Controller

**File**: `modules/metro/presentation/controller/MetroController.java`

```java
package metro.ExoticStamp.modules.metro.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.common.response.ApiResponse;
import metro.ExoticStamp.modules.metro.application.service.StationQueryService;
import metro.ExoticStamp.modules.metro.presentation.response.LineResponse;
import metro.ExoticStamp.modules.metro.presentation.response.StationListResponse;
import metro.ExoticStamp.modules.metro.presentation.response.StationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/metro")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Metro", description = "Metro network and station management")
public class MetroController {
    
    private final StationQueryService queryService;
    
    // ==================== LINE ENDPOINTS ====================
    
    @GetMapping("/lines")
    @Operation(
        summary = "Get all metro lines",
        description = "Retrieve all metro lines (cached)"
    )
    @ApiResponse(responseCode = "200", description = "Success")
    public ResponseEntity<ApiResponse<List<LineResponse>>> getAllLines() {
        log.info("GET /api/v1/metro/lines");
        
        List<LineResponse> lines = queryService.getAllLines();
        
        return ResponseEntity.ok(
            ApiResponse.success(lines, "Lines retrieved successfully")
        );
    }
    
    @GetMapping("/lines/active")
    @Operation(
        summary = "Get active metro lines",
        description = "Retrieve only active metro lines (cached)"
    )
    @ApiResponse(responseCode = "200", description = "Success")
    public ResponseEntity<ApiResponse<List<LineResponse>>> getActiveLines() {
        log.info("GET /api/v1/metro/lines/active");
        
        List<LineResponse> lines = queryService.getActiveLines();
        
        return ResponseEntity.ok(
            ApiResponse.success(lines, "Active lines retrieved successfully")
        );
    }
    
    @GetMapping("/lines/{lineId}")
    @Operation(
        summary = "Get line by ID",
        description = "Retrieve a specific metro line by ID"
    )
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "404", description = "Line not found")
    public ResponseEntity<ApiResponse<LineResponse>> getLineById(
        @PathVariable UUID lineId,
        @Parameter(description = "Line ID", required = true)
        @Schema(example = "550e8400-e29b-41d4-a716-446655440000")
        String lineIdParam
    ) {
        log.info("GET /api/v1/metro/lines/{}", lineId);
        
        LineResponse line = queryService.getLineById(lineId);
        
        return ResponseEntity.ok(
            ApiResponse.success(line, "Line retrieved successfully")
        );
    }
    
    // ==================== STATION ENDPOINTS ====================
    
    @GetMapping("/lines/{lineId}/stations")
    @Operation(
        summary = "Get stations by line",
        description = "Retrieve all active stations in a metro line (cached)"
    )
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "404", description = "Line not found")
    public ResponseEntity<ApiResponse<StationListResponse>> getStationsByLine(
        @PathVariable UUID lineId
    ) {
        log.info("GET /api/v1/metro/lines/{}/stations", lineId);
        
        StationListResponse response = queryService.getStationsByLine(lineId);
        
        return ResponseEntity.ok(
            ApiResponse.success(response, "Stations retrieved successfully")
        );
    }
    
    // ==================== HOT-PATH: NFC/QR SCAN ====================
    
    @GetMapping("/stations/scan/nfc/{nfcTag}")
    @Operation(
        summary = "Resolve station by NFC tag ⭐ HOT-PATH",
        description = "Scan and resolve station by NFC tag ID. Used in stamp collection flow.",
        tags = {"Collection"}  // relates to collection module
    )
    @ApiResponse(responseCode = "200", description = "Station resolved")
    @ApiResponse(responseCode = "404", description = "Station not found")
    @ApiResponse(responseCode = "400", description = "Station inactive")
    public ResponseEntity<ApiResponse<StationResponse>> resolveStationByNfc(
        @PathVariable String nfcTag,
        @Parameter(description = "NFC tag ID", required = true, example = "NFC_TAG_001")
        @Schema(example = "NFC_TAG_001")
        String nfcTagParam
    ) {
        log.info("GET /api/v1/metro/stations/scan/nfc/{}", nfcTag);
        
        StationResponse station = queryService.resolveStationByNfc(nfcTag);
        
        return ResponseEntity.ok(
            ApiResponse.success(station, "Station resolved successfully")
        );
    }
    
    @GetMapping("/stations/scan/qr/{qrToken}")
    @Operation(
        summary = "Resolve station by QR token ⭐ HOT-PATH",
        description = "Scan and resolve station by QR code token. Used in stamp collection flow.",
        tags = {"Collection"}  // relates to collection module
    )
    @ApiResponse(responseCode = "200", description = "Station resolved")
    @ApiResponse(responseCode = "404", description = "Station not found")
    @ApiResponse(responseCode = "400", description = "Station inactive")
    public ResponseEntity<ApiResponse<StationResponse>> resolveStationByQr(
        @PathVariable String qrToken,
        @Parameter(description = "QR code token", required = true, example = "QR_TOKEN_ABC123")
        @Schema(example = "QR_TOKEN_ABC123")
        String qrTokenParam
    ) {
        log.info("GET /api/v1/metro/stations/scan/qr/{}", qrToken);
        
        StationResponse station = queryService.resolveStationByQr(qrToken);
        
        return ResponseEntity.ok(
            ApiResponse.success(station, "Station resolved successfully")
        );
    }
}
```

---

## PHASE 5: TESTING

### Step 5.1: Unit Tests (Domain Service)

**File**: `modules/metro/domain/service/MetroDomainServiceTest.java`

```java
package metro.ExoticStamp.modules.metro.domain.service;

import metro.ExoticStamp.modules.metro.domain.exception.StationInactiveException;
import metro.ExoticStamp.modules.metro.domain.model.Station;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MetroDomainServiceTest {
    
    private MetroDomainService service;
    private Station activeStation;
    private Station inactiveStation;
    
    @BeforeEach
    void setUp() {
        service = new MetroDomainService(null, null);  // domain service doesn't need repo for these tests
        
        activeStation = Station.create(
            UUID.randomUUID(),
            "Central Station",
            "NFC_001",
            "QR_001",
            10.77225410,
            106.69880027
        );
        
        inactiveStation = Station.create(
            UUID.randomUUID(),
            "Closed Station",
            "NFC_002",
            "QR_002",
            10.77225410,
            106.69880027
        );
        inactiveStation.deactivate();
    }
    
    @Test
    void validateStationForScan_activeStation_succeeds() {
        assertDoesNotThrow(() -> service.validateStationForScan(activeStation));
    }
    
    @Test
    void validateStationForScan_inactiveStation_throws() {
        assertThrows(StationInactiveException.class, 
            () -> service.validateStationForScan(inactiveStation)
        );
    }
    
    @Test
    void validateGpsProximity_withinRadius_returnsTrue() {
        boolean result = service.validateGpsProximity(
            activeStation,
            10.77225410,  // same as station
            106.69880027, // same as station
            50.0          // 50 meter radius
        );
        
        assertTrue(result);
    }
    
    @Test
    void validateGpsProximity_noGpsData_returnsTrue() {
        // If GPS data not provided, skip check
        boolean result = service.validateGpsProximity(
            activeStation,
            null,   // no GPS data
            null,
            null
        );
        
        assertTrue(result);
    }
    
    @Test
    void validateGpsProximity_outsideRadius_returnsFalse() {
        boolean result = service.validateGpsProximity(
            activeStation,
            10.80,  // ~3km away
            106.69880027,
            50.0    // 50 meter radius
        );
        
        assertFalse(result);
    }
}
```

### Step 5.2: Integration Tests (Repository)

**File**: `modules/metro/infrastructure/repository/StationRepositoryAdapterTest.java`

```java
package metro.ExoticStamp.modules.metro.infrastructure.repository;

import metro.ExoticStamp.modules.metro.domain.model.Station;
import metro.ExoticStamp.modules.metro.infrastructure.persistence.JpaStationRepository;
import metro.ExoticStamp.modules.metro.infrastructure.persistence.StationEntity;
import metro.ExoticStamp.modules.metro.application.mapper.StationAppMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class StationRepositoryAdapterTest {
    
    @Autowired
    private TestEntityManager em;
    
    @Autowired
    private JpaStationRepository jpaRepository;
    
    private StationRepositoryAdapter adapter;
    private StationAppMapper mapper;
    
    @BeforeEach
    void setUp() {
        mapper = new StationAppMapper();
        adapter = new StationRepositoryAdapter(jpaRepository, mapper);
    }
    
    @Test
    void findByNfcTagId_existingTag_returnsStation() {
        // Arrange
        UUID lineId = UUID.randomUUID();
        StationEntity entity = StationEntity.builder()
            .id(UUID.randomUUID())
            .lineId(lineId)
            .name("Central Station")
            .nfcTagId("NFC_TAG_001")
            .qrCodeToken("QR_001")
            .latitude(10.77225410)
            .longitude(106.69880027)
            .isActive(true)
            .build();
        
        em.persistAndFlush(entity);
        
        // Act
        Optional<Station> result = adapter.findByNfcTagId("NFC_TAG_001");
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals("Central Station", result.get().getName());
        assertEquals("NFC_TAG_001", result.get().getNfcTagId());
    }
    
    @Test
    void findByNfcTagId_nonExistentTag_returnsEmpty() {
        // Act
        Optional<Station> result = adapter.findByNfcTagId("NONEXISTENT");
        
        // Assert
        assertTrue(result.isEmpty());
    }
    
    @Test
    void save_newStation_persists() {
        // Arrange
        Station station = Station.create(
            UUID.randomUUID(),
            "New Station",
            "NFC_NEW",
            "QR_NEW",
            10.77,
            106.70
        );
        
        // Act
        Station saved = adapter.save(station);
        em.flush();
        
        // Assert
        assertNotNull(saved.getId());
        Optional<Station> retrieved = adapter.findById(saved.getId());
        assertTrue(retrieved.isPresent());
        assertEquals("New Station", retrieved.get().getName());
    }
}
```

### Step 5.3: API Tests (Controller)

**File**: `modules/metro/presentation/controller/MetroControllerTest.java`

```java
package metro.ExoticStamp.modules.metro.presentation.controller;

import metro.ExoticStamp.modules.metro.application.service.StationQueryService;
import metro.ExoticStamp.modules.metro.domain.exception.StationNotFoundException;
import metro.ExoticStamp.modules.metro.presentation.response.StationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MetroController.class)
class MetroControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private StationQueryService queryService;
    
    @Test
    void resolveStationByNfc_validTag_returns200() throws Exception {
        // Arrange
        UUID stationId = UUID.randomUUID();
        StationResponse mockResponse = StationResponse.builder()
            .id(stationId)
            .name("Central Station")
            .nfcTagId("NFC_TAG_001")
            .latitude(10.77225410)
            .longitude(106.69880027)
            .isActive(true)
            .build();
        
        when(queryService.resolveStationByNfc("NFC_TAG_001"))
            .thenReturn(mockResponse);
        
        // Act & Assert
        mockMvc.perform(get("/api/v1/metro/stations/scan/nfc/NFC_TAG_001")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.data.name").value("Central Station"))
            .andExpect(jsonPath("$.data.nfcTagId").value("NFC_TAG_001"));
    }
    
    @Test
    void resolveStationByNfc_invalidTag_returns404() throws Exception {
        // Arrange
        when(queryService.resolveStationByNfc("INVALID"))
            .thenThrow(new StationNotFoundException("INVALID", "NFC"));
        
        // Act & Assert
        mockMvc.perform(get("/api/v1/metro/stations/scan/nfc/INVALID")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error.code").value("STATION_NOT_FOUND"));
    }
}
```

---

## PHASE 6: CONFIG UPDATES

### Update application.yml

```yaml
# application.yml
cache:
  ttl:
    metro-lines: 1800      # 30 minutes
    metro-stations: 1800   # 30 minutes
    user-data: 1800        # 30 minutes
```

### Update pom.xml (if needed)

```xml
<!-- Already should have these, but verify: -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>
```

---

## NEXT STEPS AFTER METRO

✅ Metro module done →  **Collection module** next (phụ thuộc Metro)

Collection will use:
- `StationQueryService.resolveStationByNfc()`
- `StationQueryService.resolveStationByQr()`

As entry points to collect stamp flow.

---

**Now, follow these steps line-by-line to implement Metro module! 🚀**

Are you ready to start? Or do you have questions before implementation?
