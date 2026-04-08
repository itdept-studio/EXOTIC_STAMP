# CURSOR PROMPT - IMPLEMENT METRO MODULE

Copy/paste the prompt below into Cursor:

```text
You are implementing the `metro` module for an existing Spring Boot backend.

Project root: d:\code\exotic_stamp
Language/stack: Java 21, Spring Boot 3.3.5, Spring Data JPA, Redis, Flyway, Maven.
Architecture style already used in this repo: Spring-pragmatic DDD with module layers.

IMPORTANT: Use the current codebase as source of truth, not generic templates.

Read these files first:
1) docs/EXOTIC_STAMP_DEPLOYMENT_PLAN.md
2) docs/IMPLEMENTATION_GUARDRAILS.md
3) docs/METRO_IMPLEMENTATION_GUIDE.md
4) src/main/resources/db/migration/V2__metro.sql
5) Existing implemented modules for style reference:
   - modules/user/*
   - modules/auth/*
   - modules/rbac/*
   - infra/cache/BaseCacheRepository.java
   - common/exceptions/GlobalExceptionHandler.java

Then implement the Metro module with the constraints below.

====================================================
GOAL
====================================================
Implement `modules/metro` for read/query use cases (MVP foundation for scan lookup):
- Get all lines
- Get line by id
- Get stations by line
- Resolve station by NFC tag (hot path)
- Resolve station by QR token (hot path)

====================================================
CRITICAL ALIGNMENT RULES (MUST FOLLOW)
====================================================
1) Follow existing project patterns from user/auth/rbac modules.
2) Do NOT introduce a separate "domain model" + "entity" split for Metro if it breaks existing style.
   - In this repo, domain model classes are directly `@Entity` in `domain/model` (see User, Role, Permission).
3) Respect V2 schema as-is:
   - `lines.id` and `stations.id` are SERIAL => use Integer IDs (NOT UUID).
   - Fields/constraints must map to V2 columns.
4) Keep dependency direction:
   - presentation -> application -> domain <- infrastructure
5) Query services must be `@Transactional(readOnly=true)`.
6) Keep naming and packaging consistent with existing modules.
7) Do not break existing code.

====================================================
IMPLEMENTATION SCOPE
====================================================
Create Metro module structure consistent with current repo style:

modules/metro/
- application/
  - MetroQueryService.java
  - mapper/MetroAppMapper.java
- domain/
  - model/Line.java
  - model/Station.java
  - repository/LineRepository.java
  - repository/StationRepository.java
  - exception/LineNotFoundException.java
  - exception/StationNotFoundException.java
  - exception/StationInactiveException.java
- infrastructure/
  - persistence/JpaLineRepository.java
  - persistence/JpaStationRepository.java
  - persistence/LineRepositoryAdapter.java
  - persistence/StationRepositoryAdapter.java
  - cache/StationScanCacheRepository.java (custom cache by String keys for nfc/qr)
- presentation/
  - MetroController.java
  - dto/response/LineResponse.java
  - dto/response/StationResponse.java

If you need additional helper files (query DTOs, mapper helpers), add them minimally and consistently.

====================================================
DATABASE MAPPING REQUIREMENTS (FROM V2__metro.sql)
====================================================
Line (`lines`):
- id: Integer (PK, generated identity)
- code: String (unique, not null, max 10)
- name: String (not null, max 100)
- color: String (max 7)
- totalStations: Integer (not null, default 0)
- isActive: Boolean
- createdBy, createdAt, updatedBy, updatedAt

Station (`stations`):
- id: Integer (PK, generated identity)
- lineId: Integer (FK -> lines.id)
- code: String (unique, not null, max 20)
- name: String (not null, max 100)
- sequence: Integer (not null)
- description: String (max 500)
- historicalInfo: String (TEXT)
- imageUrl: String (max 255)
- latitude: BigDecimal or Double mapping compatible with DECIMAL(9,6)
- longitude: BigDecimal or Double mapping compatible with DECIMAL(9,6)
- nfcTagId: String (unique nullable)
- qrCodeToken: String (unique nullable)
- collectorCount: Integer (default 0)
- isActive: Boolean
- audit columns

Keep column names and table names exactly aligned.

====================================================
QUERY USE CASES
====================================================
In `MetroQueryService`, implement:
- List<LineResponse> getAllLines(boolean activeOnly)
- LineResponse getLineById(Integer lineId)
- List<StationResponse> getStationsByLine(Integer lineId, boolean activeOnly)
- StationResponse resolveStationByNfc(String nfcTagId)
- StationResponse resolveStationByQr(String qrCodeToken)

Behavior:
- resolve by NFC/QR must throw StationNotFoundException if not found.
- if station is inactive, throw StationInactiveException.
- For line/station by id not found, throw corresponding not-found exception.

====================================================
CACHING REQUIREMENTS
====================================================
- Hot path scan lookups should use cache-aside.
- Because `BaseCacheRepository` is UUID-keyed, create a metro-specific cache component for String keys:
  - keys:
    - station:nfc:{nfcTagId}
    - station:qr:{qrCodeToken}
- Use RedisTemplate directly in this custom cache class.
- Handle cache failures gracefully (log warning, fallback to DB).
- Do NOT change BaseCacheRepository signature globally.

====================================================
API ENDPOINTS
====================================================
Create controller: `@RequestMapping("/api/v1/metro")`

Endpoints:
- GET /lines
  - optional query param: activeOnly (default true)
- GET /lines/{lineId}
- GET /lines/{lineId}/stations
  - optional query param: activeOnly (default true)
- GET /stations/scan/nfc/{nfcTagId}
- GET /stations/scan/qr/{qrCodeToken}

Response format:
- Use existing `ApiResponse` wrapper pattern consistently.

====================================================
EXCEPTION HANDLING INTEGRATION
====================================================
- Add handling in GlobalExceptionHandler for metro exceptions:
  - LineNotFoundException -> 404, code `LINE_NOT_FOUND`
  - StationNotFoundException -> 404, code `STATION_NOT_FOUND`
  - StationInactiveException -> 400, code `STATION_INACTIVE`

Do this without breaking existing handlers.

====================================================
SECURITY
====================================================
- Keep current security config behavior.
- Metro endpoints can remain authenticated by default unless there is an explicit existing rule to make them public.
- Do not loosen security globally.

====================================================
TESTS (MINIMUM REQUIRED)
====================================================
Add tests at least for:
1) MetroQueryService:
   - resolve by NFC success
   - resolve by NFC not found
   - resolve by QR success
   - inactive station rejected
2) Repository adapter basic mapping behavior
3) Controller endpoint smoke tests for two scan endpoints

Use existing testing stack and conventions (JUnit5 + Mockito + Spring test).

====================================================
DELIVERABLE FORMAT
====================================================
1) Implement code changes directly.
2) Ensure project compiles.
3) Provide a concise summary including:
   - created files
   - modified files
   - key decisions made to align docs vs source-of-truth
   - what remains for next module (`collection`)

Before coding, explicitly list any mismatches you detected between:
- docs/METRO_IMPLEMENTATION_GUIDE.md
- src/main/resources/db/migration/V2__metro.sql
and explain which source you followed.
```
