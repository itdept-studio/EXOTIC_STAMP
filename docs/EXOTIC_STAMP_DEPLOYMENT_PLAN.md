# EXOTIC STAMP - DEPLOYMENT IMPLEMENTATION PLAN

> Chi tiết lộ trình triển khai các module còn lại, từ Metro đến Community.
> Cơ sở: architecture.md, PROJECT_CODEBASE_INFO.md, EXOTIC_STAMP_CONTEXT.md, working_pipeline.md

---

## PHẦN 1: PHÂN TÍCH HIỆN TRẠNG

### ✅ Modules đã hoàn thành (code nghiệp vụ)
- **auth** (47 files): login, register, refresh, verify, forgot/reset password, logout
- **user** (23 files): CRUD user, profile, validation
- **rbac** (24 files): role/permission, assign/revoke
- **Infra**: mail queue, cache base, redis, security config

### 🚧 Modules cần triển khai (schema ready, code 0%)
- **metro** (0 files)
- **collection** (0 files)
- **reward** (0 files)
- **monetization** (0 files)
- **community** (0 files)

### 📊 Database Schema (100% sẵn sàng)
```
V1: mail_jobs (✅ in use)
V2: metro (lines, stations) ← cần code
V3: collection (campaigns, campaign_stations, stamp_designs, user_stamps) ← cần code
V4: reward (partners, milestones, rewards, voucher_pool, user_rewards) ← cần code
V5: monetization (advertisements, ad_impressions, affiliate_banners, affiliate_banner_clicks) ← cần code
V6: community (referral_codes, referrals, share_events, notifications) ← cần code
```

---

## PHẦN 2: DEPENDENCY MAP & MODULE ORDER

### 2.1 Dependency Graph (sơ đồ phụ thuộc)

```
┌─ auth ──────────────────────────────────┐
│   (register, login, token, JWT)         │
└─────────────────────────────────────────┘
          ↓
┌─ user (id, email, profile) ─────────────┐
│   └─ rbac (roles, permissions)          │
└─────────────────────────────────────────┘
          ↓
    ┌──────────────────────────────────────────────────────────────┐
    │                                                              │
┌─ metro ────────────────────┐         ┌─ community ──────────────┐
│ (lines, stations, NFC/QR)  │         │ (referral, share, notif) │
└────────────────────────────┘         └──────────────────────────┘
          ↓                                     ↑
┌─ collection ───────────────────┐           │
│ (campaigns, stamps, collect)   │───────────┘
└────────────────────────────────┘
          ↓
┌─ reward ───────────────────────┐
│ (partners, milestones,         │
│  vouchers, user_rewards)       │
└────────────────────────────────┘

┌─ monetization ──────────────────┐
│ (ads, affiliates, impressions)  │ (semi-independent)
└─────────────────────────────────┘
```

### 2.2 Thứ tự triển khai hợp lý

| Thứ tự | Module | Lý do | Dự kiến timeline |
|--------|--------|------|------------------|
| 1️⃣ | **metro** | Nền tảng cho collection (lookup station by NFC/QR) | 3-4 ngày |
| 2️⃣ | **collection** | Phụ thuộc metro; core revenue (stamp collection) | 4-5 ngày |
| 3️⃣ | **reward** | Phụ thuộc collection; monetization loop | 4-5 ngày |
| 4️⃣ | **monetization** | Phụ thuộc partner (có thể song song với reward) | 3-4 ngày |
| 5️⃣ | **community** | Phụ thuộc user + collection; growth loop | 3-4 ngày |

**Tổng timeline:** 17-22 ngày (tùy song song task)

---

## PHẦN 3: DETAILED IMPLEMENTATION CHECKLIST

### ⭐ MODULE 1: METRO

**Objective**: Quản lý mạng tàu (lines + stations), hot-path lookup by NFC/QR

**Schema ready**:
- `lines` (id, name, color, description, is_active)
- `stations` (id, line_id, name, nfc_tag_id, qr_code_token, latitude, longitude, is_active, indexed)

**Tasks**:
```
1. Domain Layer
   ☐ Entity: Line, Station
   ☐ Exception: LineNotFoundException, StationNotFoundException
   ☐ Repository interface: LineRepository, StationRepository
   ☐ Domain service (optional): MetroDomainService (validation)

2. Infrastructure Layer
   ☐ JpaLineRepository, JpaStationRepository
   ☐ LineRepositoryAdapter, StationRepositoryAdapter
   ☐ Query: StationCacheRepository (Redis cache-aside pattern)

3. Application Layer
   ☐ StationQueryService (readOnly=true)
     - getAllLines()
     - getLineById(lineId)
     - getStationsByLine(lineId)
     - resolveStationByNfc(nfc_tag_id) ⭐ HOT-PATH
     - resolveStationByQr(qr_code_token) ⭐ HOT-PATH
   ☐ Mapper: StationAppMapper

4. Presentation Layer
   ☐ MetroController (/api/v1/metro)
     - GET /lines
     - GET /lines/{id}
     - GET /lines/{id}/stations
     - GET /stations/scan/nfc/{nfcTag}
     - GET /stations/scan/qr/{qrToken}
   ☐ Response DTOs: LineResponse, StationResponse

5. Testing
   ☐ Unit test: repository adapter, query service
   ☐ Integration test: hot-path queries (performance)
   ☐ API test: scan endpoints
```

**Key points**:
- `nfc_tag_id` + `qr_code_token` MUST được indexed (production hot-path)
- Dùng cache-aside để tránh N+1 khi load stations by line
- Query service readOnly=true

---

### ⭐ MODULE 2: COLLECTION

**Objective**: Stamp collection flow (campaigns + user stamp collect + stamp book progression)

**Schema ready**:
- `campaigns` (id, name, description, line_id, partner_id, start_at, end_at, is_active)
- `campaign_stations` (campaign_id, station_id) [pivot]
- `stamp_designs` (id, campaign_id, station_id, design_image_url, description)
- `user_stamps` (id, user_id, station_id, campaign_id, stamp_design_id, collected_at, collected_lat, collected_lng, device_fingerprint)
  - Unique constraint: (user_id, station_id, campaign_id) NULLS NOT DISTINCT

**Tasks**:
```
1. Domain Layer
   ☐ Entities: Campaign, CampaignStation, StampDesign, UserStamp
   ☐ Value Object (optional): StampBookProgress
   ☐ Exceptions: CampaignNotFoundException, StampAlreadyCollectedException, CampaignExpiredException
   ☐ Repository interfaces: CampaignRepository, UserStampRepository
   ☐ Domain service: CollectionDomainService
     - validateStampCollection(userId, stationId, campaignId) → throws if duplicate
     - evaluateMilestone(userId, stationId) → milestone achieved?

2. Infrastructure Layer
   ☐ JpaCampaignRepository, JpaUserStampRepository
   ☐ CampaignRepositoryAdapter, UserStampRepositoryAdapter
   ☐ Redis cache: CampaignCacheRepository (campaign list, active campaigns)

3. Application Layer
   ☐ CollectionCommandService @Transactional
     - collectStamp(userId, stationId, campaignId, deviceFingerprint, gps) 
       → return StampCollectResponse with milestone info
       → trigger cache invalidate on user's stamp book
   ☐ CollectionQueryService readOnly=true
     - getStampBook(userId, lineId) → progress per station
     - getCampaignsList() → active campaigns
     - getUserStamps(userId) → all stamps collected
   ☐ Mapper: CampaignAppMapper, UserStampAppMapper

4. Presentation Layer
   ☐ CollectionController (/api/v1/collection)
     - POST /collect (⭐ core endpoint)
       Input: { stationId, campaignId?, deviceFingerprint, latitude, longitude }
       Output: StampCollectResponse { stampId, stampDesign, milestone?, nextMilestoneProgress }
     - GET /campaigns
     - GET /stamp-book/{userId}/{lineId}
     - GET /my-stamps
   ☐ Request/Response DTOs

5. Testing
   ☐ Domain rule: duplicate collect rejection
   ☐ Anti-cheat: GPS validation (optional: distance from station)
   ☐ API flow: collect → check milestone
```

**Key points**:
- `collectStamp` là CORE endpoint - phải optimization được, index on hot-path lookup
- Unique constraint + DB-level anti-cheat là phòng tuyến đầu
- Sau collect phải cache-evict user's stamp book để next GET lấy data fresh
- GPS validation là optional nhưng recommended (anti-cheat remote collection)

---

### ⭐ MODULE 3: REWARD

**Objective**: Milestone evaluation + reward/voucher issuance + user reward tracking

**Schema ready**:
- `partners` (id, name, logo_url, contact_email)
- `milestones` (id, campaign_id, collection_count, reward_id, is_active)
- `rewards` (id, partner_id, name, description, reward_type, [points/discount], is_active)
- `voucher_pool` (id, reward_id, code, is_redeemed, redeemed_by_user_id, redeemed_at)
- `user_rewards` (id, user_id, reward_id, milestone_id, voucher_id, received_at)
  - Unique constraint: (user_id, milestone_id) NULLS NOT DISTINCT

**Tasks**:
```
1. Domain Layer
   ☐ Entities: Partner, Milestone, Reward, VoucherPool, UserReward
   ☐ Value Object (optional): RewardInfo, VoucherInfo
   ☐ Exceptions: RewardAlreadyReceivedException, NoAvailableVouchersException
   ☐ Repository interfaces: MilestoneRepository, RewardRepository, VoucherRepository, UserRewardRepository
   ☐ Domain service: RewardDomainService
     - evaluateMilestone(userId, campaignId) → check if milestone hit
     - issueReward(userId, milestoneId) → allocate reward, reserve voucher

2. Infrastructure Layer
   ☐ Jpa repositories + Adapters (all 4)
   ☐ VoucherLockingRepository (để lock + allocate voucher safely)
     - findAndReserveVoucher(rewardId) → locked query, atomic claim

3. Application Layer
   ☐ RewardCommandService @Transactional
     - evaluateAndIssueReward(userId, campaignId)
       → check milestone → issue reward → allocate voucher → create user_reward
       → trigger notification
     - redeemVoucher(userId, voucherId) → validate + mark redeemed
   ☐ RewardQueryService readOnly=true
     - getUserRewards(userId) → list all received rewards
     - getRewardDetails(rewardId)
   ☐ Mapper: RewardAppMapper

4. Presentation Layer
   ☐ RewardController (/api/v1/rewards)
     - GET /my-rewards (user's received rewards)
     - GET /{rewardId}
     - POST /redeem (if applicable)
   ☐ Event: PostCollectStampEvent (trigger from collection service)
     → Listener: RewardEvaluationListener → evaluateAndIssueReward

5. Testing
   ☐ Domain: ensure one-reward-per-milestone
   ☐ Voucher allocation: concurrent voucher claim test (race condition)
   ☐ Rollback: if voucher exhausted → proper error
```

**Key points**:
- Milestone evaluation TRIGGERED từ collection (không synchronous, async listener better)
- Voucher allocation phải atomic + locked (nếu concurrent access cao)
- One-reward-per-milestone = unique constraint + domain service double-check
- Notification event sau reward issued

---

### ⭐ MODULE 4: MONETIZATION

**Objective**: Ad/affiliate tracking, impression/click ingestion, revenue analytics

**Schema ready**:
- `advertisements` (id, partner_id, campaign_id, name, image_url, cta_url, ad_type, cpm/cpc, is_active, start_at, end_at)
- `ad_impressions` (id, advertisement_id, station_id, user_id, impression_at, device_fingerprint)
- `affiliate_banners` (id, partner_id, name, image_url, cta_url, commission_rate)
- `affiliate_banner_clicks` (id, banner_id, user_id, clicked_at, device_fingerprint)

**Tasks**:
```
1. Domain Layer
   ☐ Entities: Advertisement, AdImpression, AffiliateBanner, AffiliateBannerClick
   ☐ Exceptions: AdNotFoundException, InvalidAdTypeException
   ☐ Repository interfaces: AdRepository, AdImpressionRepository, BannerRepository
   ☐ Domain service (optional): MonetizationDomainService
     - selectAdForSlot(context) → choose best ad based on rules
     - calculateCost(ad, impression_count) → CPM/CPC logic

2. Infrastructure Layer
   ☐ Jpa repositories + Adapters
   ☐ AdImpressionBatchRepository (bulk insert untuk high-volume)

3. Application Layer
   ☐ MonetizationCommandService @Transactional
     - trackAdImpression(adId, deviceFingerprint, stationId)
     - trackAdClick(adId, deviceFingerprint)
     - trackBannerClick(bannerId, userId, deviceFingerprint)
   ☐ MonetizationQueryService readOnly=true
     - getActiveAds(context) → slot-based selection
     - getAdAnalytics(adId, startDate, endDate) → impressions, clicks, revenue
     - getAffiliateAnalytics(partnerId)
   ☐ Mapper: AdAppMapper

4. Presentation Layer
   ☐ MonetizationController (/api/v1/monetization)
     - GET /ads/slots/{slotType}?context={context}
     - POST /ads/{adId}/impression
     - POST /ads/{adId}/click
     - POST /banners/{bannerId}/click
     - GET /analytics/ads/{adId}
     - GET /analytics/partner/{partnerId}
   ☐ Response DTOs: AdResponse, AnalyticsResponse

5. Testing
   ☐ High-volume impression tracking test
   ☐ Duplicate impression detection
   ☐ Analytics aggregation accuracy
```

**Key points**:
- Impression/click tracking là HIGH-VOLUME (index: advertisement_id, created_at)
- Batch insert better thay vì one-by-one
- Optional: event streaming / async processing cho analytics
- CPM/CPC calculation logic phải clear và testable

---

### ⭐ MODULE 5: COMMUNITY

**Objective**: Referral program, share events, in-app notifications, growth loop

**Schema ready**:
- `referral_codes` (id, user_id, code, created_at, is_active)
- `referrals` (id, referral_code_id, referred_user_id, status, reward_received_at)
- `share_events` (id, user_id, stamp_id, platform, shared_at)
- `notifications` (id, user_id, type, title, message, is_read, created_at)

**Tasks**:
```
1. Domain Layer
   ☐ Entities: ReferralCode, Referral, ShareEvent, Notification
   ☐ Enums: ReferralStatus (PENDING, COMPLETED, REWARDED), NotificationType
   ☐ Exceptions: ReferralCodeNotFoundException, InvalidReferralException, SelfReferralException
   ☐ Repository interfaces: ReferralCodeRepository, ReferralRepository, NotificationRepository
   ☐ Domain service: CommunityDomainService
     - createReferralCode(userId) → gen unique code
     - processReferral(referralCode, newUserId) → validate, create referral
     - triggerReferralReward(referralId) → when referred completes condition

2. Infrastructure Layer
   ☐ Jpa repositories + Adapters (all 4)
   ☐ NotificationRepository optimize for: user_id + is_read queries

3. Application Layer
   ☐ CommunityCommandService @Transactional
     - generateReferralCode(userId)
     - registerViaReferral(referralCode, newUserData)
     - completeReferralCondition(referralId) → activate reward
     - shareStamp(userId, stampId, platform) → track share event
     - createNotification(userId, type, message)
     - markNotificationRead(notificationId)
   ☐ CommunityQueryService readOnly=true
     - getMyReferralCode(userId)
     - getReferralHistory(userId)
     - getNotifications(userId, pagination)
     - getShareStats(stampId)
   ☐ Mapper: CommunityAppMapper

4. Presentation Layer
   ☐ CommunityController (/api/v1/community)
     - GET /my-referral-code
     - POST /register-referral (during auth flow, or separate)
     - GET /referral-history
     - POST /share/{stampId}
     - GET /notifications
     - POST /notifications/{notificationId}/read
   ☐ Request/Response DTOs

5. Testing
   ☐ Self-referral prevention
   ☐ Duplicate referral prevention (one referred user = one referring user)
   ☐ Reward trigger on condition completion
```

**Key points**:
- Referral code generation: dùng short unique code (vd: 6-8 char alphanumeric)
- Referral reward trigger: có thể là async event listener (post-verify email, post-first collect)
- Notification: basic FIFO queue, có thể escalate to push notification service sau
- Share tracking: simple event log, dùng cho viral coefficient analytics

---

## PHẦN 4: ARCHITECTURE GUARDRAILS (APPLY CHO TẤT CẢ MODULE)

### 4.1 Layer structure (mỗi module)

```
modules/
  ├─ {moduleName}/
      ├─ domain/
      │   ├─ entity/
      │   ├─ repository/          (interface chỉ)
      │   ├─ service/              (domain service)
      │   ├─ exception/
      │   └─ event/                (domain event)
      ├─ application/
      │   ├─ command/              (command object)
      │   ├─ query/                (query object)
      │   ├─ service/              (command/query service)
      │   ├─ mapper/
      │   └─ port/                 (outbound interface)
      ├─ infrastructure/
      │   ├─ repository/           (JPA + adapter)
      │   ├─ cache/
      │   ├─ event/                (event listener)
      │   └─ integration/
      └─ presentation/
          ├─ controller/
          ├─ request/
          ├─ response/
          └─ exception/
```

### 4.2 Code style & naming

- Entity: `{Name}Entity` or just `{Name}`
- Repository interface: `{Name}Repository`
- JPA repository: `Jpa{Name}Repository`
- Adapter: `{Name}RepositoryAdapter`
- Service (command): `{Name}CommandService` → `@Transactional`
- Service (query): `{Name}QueryService` → `@Transactional(readOnly=true)`
- Exception: `{Name}NotFoundException`, `{Reason}Exception`
- DTO request: `{Action}{Name}Request`
- DTO response: `{Name}Response`
- Mapper: `{Name}AppMapper`

### 4.3 Critical rules

```java
// ✅ DO:
@Service
@Transactional
public class StampCommandService {
    private final StampRepository repository;     // domain interface
    private final StampMapper mapper;
    
    public StampCollectResponse collect(CollectStampCommand cmd) {
        // domain validation
        // repository.save() through adapter
        // cache invalidate
    }
}

// ❌ DON'T:
@Service
public class StampService {
    private final JpaStampRepository jpaRepo;    // ❌ DIRECT JPA
    
    public void collect(Map data) {              // ❌ NO MAP/DATA
        jpaRepo.save(...);                        // ❌ NO @TRANSACTIONAL
    }
}
```

### 4.4 Write flow pattern

```
Request → Controller (@Valid DTO)
        → CommandService @Transactional
            → Domain validation
            → Repository.save() (through adapter)
            → Cache evict/invalidate (if needed)
            → Event publish (if needed)
        → Mapper to Response DTO
        → Return 200/201
```

### 4.5 Read flow pattern

```
Request → Controller
        → QueryService @Transactional(readOnly=true)
            → Check cache first (cache-aside pattern)
            → If miss: Repository.findBy...()
            → Put to cache (with TTL)
        → Mapper to Response DTO
        → Return 200
```

---

## PHẦN 5: QUICK REFERENCE - MODULE INTERDEPENDENCY

### Hợp tác giữa modules (khi code)

| From | To | How | Example |
|------|----|----|---------|
| collection | metro | Query (read-only) | `StationQueryService.resolveStationByNfc()` |
| reward | collection | Event (async) | PostCollectStampEvent → RewardEvaluationListener |
| monetization | metro / campaign | Query (read-only) | Ad slot selection by station/campaign context |
| community | user | Event (async) | Referral registration → NotificationService |
| community | collection | Query (read-only) | Share stats per stamp |

### Cross-module communication

- **Same app**: Event publisher/listener (Spring event)
- **Between modules**: Interface + adapter pattern
- **NO DIRECT @Autowired between modules** (tránh tight coupling)

---

## PHẦN 6: TESTING STRATEGY

### Unit tests
- Domain service rules
- Mapper transformations
- Exception handling

### Integration tests
- Repository adapter + JPA queries
- Cache behavior
- Database constraint violations

### API (E2E) tests
```
Auth flow:
  ☐ Register + verify email
  ☐ Login + get JWT
  ☐ Refresh token

Collection flow:
  ☐ Scan station (NFC)
  ☐ Collect stamp (success)
  ☐ Collect duplicate stamp (fail)
  ☐ Check milestone → receive reward

Reward flow:
  ☐ Evaluate milestone after stamp count
  ☐ Issue reward + voucher allocation
  ☐ Ensure one-reward-per-milestone

Monetization flow:
  ☐ Track ad impression
  ☐ Track ad click
  ☐ Verify analytics data

Community flow:
  ☐ Generate referral code
  ☐ Register via referral
  ☐ Self-referral rejection
  ☐ Referral reward trigger
```

### Load/Performance tests
- Scan-to-stamp hot-path (target: <100ms p95)
- Ad impression bulk tracking
- Cache hit rate monitoring

---

## PHẦN 7: DEPLOYMENT & ROLLBACK

### Pre-deployment
- [ ] Schema migration tested on staging
- [ ] API contract reviewed (OpenAPI/Swagger)
- [ ] E2E tests all pass
- [ ] Monitoring/alerting configured

### Deployment order
1. Apply Flyway migrations
2. Deploy service (blue-green or canary)
3. Smoke test core flows
4. Monitor 24h

### Rollback
- **DB migration**: Flyway supports undo (V{n}__Undo) if needed
- **Code**: Standard Git revert

---

## PHẦN 8: TIMELINE ESTIMATE (Detailed breakdown)

### Metro: 3-4 ngày
- Day 1: Domain + infrastructure layer
- Day 2: Application layer + mapper
- Day 3: Controller + DTOs + tests
- Day 4 (optional): Performance tuning + cache optimization

### Collection: 4-5 ngày
- Day 1: Domain + anti-cheat logic
- Day 2: Infrastructure + cache
- Day 3: Application service
- Day 4: Controller + API tests
- Day 5 (optional): GPS validation + advanced features

### Reward: 4-5 ngày
- Day 1: Domain + milestone logic
- Day 2: Voucher allocation (locking/concurrency)
- Day 3: Command service + event listener
- Day 4: Controller + analytics
- Day 5 (optional): Performance tuning

### Monetization: 3-4 ngày
- Day 1: Domain + entities
- Day 2: Infrastructure + bulk tracking
- Day 3: Query service + analytics
- Day 4 (optional): Real-time analytics dashboard API

### Community: 3-4 ngày
- Day 1: Domain + referral logic
- Day 2: Notification system
- Day 3: Commands + events
- Day 4 (optional): Share analytics

**Total: 17-22 ngày** (có thể song song: metro + monetization, collection + community)

---

## PHẦN 9: NEXT STEPS

1. **Confirm Plan** (Clarify điểm nào cần điều chỉnh)
2. **Start Metro** (Foundation module)
3. **Implement step-by-step** (Tôi sẽ guide code ở mỗi layer)
4. **Test + Review**
5. **Move to Collection**
6. ...
