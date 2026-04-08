# PROJECT_CODEBASE_INFO

> Snapshot theo hiện trạng codebase tại `d:\code\exotic_stamp`.

---

## 1. Kiến trúc dự án

### 1.1 Dự án Web hay Mobile?

- Đây là **backend API server** (Spring Boot), không phải frontend Web SPA (React/Vue/Angular) và cũng không phải mobile app (React Native).
- Backend này phục vụ cho client app/web bên ngoài thông qua REST API.

### 1.2 Stack công nghệ chính

- **Language/Runtime**: Java 21
- **Framework**: Spring Boot 3.3.5
- **Build tool**: Maven
- **Web/API**: Spring MVC (`spring-boot-starter-web`) + WebFlux dependency cho WebClient
- **Security**: Spring Security, OAuth2 client/resource server, JWT (`jjwt 0.12.6`)
- **ORM/Data**: Spring Data JPA + Hibernate
- **Migration**: Flyway
- **Database driver**: PostgreSQL (runtime), MySQL driver cũng có trong pom nhưng datasource hiện tại dùng Postgres
- **Cache/Session state**: Redis
- **Mail**: Spring Mail + custom mail queue
- **OpenAPI**: springdoc-openapi
- **Validation**: Jakarta Bean Validation (`spring-boot-starter-validation`)
- **Object mapping**: MapStruct + ModelMapper
- **Testing**: Spring Boot Test, Mockito, Spring Security Test

### 1.3 Folder structure hiện tại (rút gọn)

```text
src/
  main/
    java/metro/ExoticStamp/
      config/
      common/
      infra/
      integration/
      modules/
        auth/
        user/
        rbac/
        metro/          (empty)
        collection/     (empty)
        reward/         (empty)
        monetization/   (empty)
        community/      (empty)
    resources/
      application.yml
      application-dev.yml
      application-prod.yml
      db/migration/
        V1__create_mail_jobs_table.sql
        V2__metro.sql
        V3__collection.sql
        V4__reward.sql
        V5__monetization.sql
        V6__community.sql
```

---

## 2. Database & Schema

## 2.1 Danh sách bảng

### 2.1.1 Bảng từ JPA entity hiện có

- `users`
- `access_tokens`
- `audit_logs`
- `roles`
- `permissions`
- `user_roles`
- `role_permissions`
- `mail_jobs`

### 2.1.2 Bảng từ Flyway migration V2-V6

- `lines`
- `stations`
- `campaigns`
- `campaign_stations`
- `stamp_designs`
- `user_stamps`
- `partners`
- `milestones`
- `rewards`
- `voucher_pool`
- `user_rewards`
- `advertisements`
- `ad_impressions`
- `affiliate_banners`
- `affiliate_banner_clicks`
- `referral_codes`
- `referrals`
- `share_events`
- `notifications`

## 2.2 Relationships chính

- `lines` 1:N `stations`
- `campaigns` N:N `stations` qua `campaign_stations`
- `stations` 1:N `stamp_designs` (nullable)
- `campaigns` 1:N `stamp_designs` (nullable)
- `users` (logical) 1:N `user_stamps` (lưu `user_id` UUID, không FK)
- `stamp_designs` 1:N `user_stamps`
- `stations` 1:N `user_stamps`
- `campaigns` 1:N `user_stamps` (nullable)
- `milestones` 1:N `rewards`
- `partners` 1:N `rewards`
- `rewards` 1:N `voucher_pool`
- `users` (logical) 1:N `user_rewards` (lưu `user_id` UUID, không FK)
- `rewards` 1:N `user_rewards`
- `milestones` 1:N `user_rewards`
- `voucher_pool` 1:N `user_rewards` (nullable)
- `partners` 1:N `advertisements`
- `campaigns` 1:N `advertisements` (nullable)
- `advertisements` 1:N `ad_impressions`
- `stations` 1:N `ad_impressions` (nullable)
- `partners` 1:N `affiliate_banners`
- `affiliate_banners` 1:N `affiliate_banner_clicks`
- `referral_codes` 1:N `referrals`
- `user_stamps` 1:N `share_events` (nullable `stamp_id`)
- RBAC:
  - `roles` 1:N `user_roles`
  - `roles` 1:N `role_permissions`
  - `permissions` 1:N `role_permissions`

## 2.3 PK / FK / Index (tóm tắt)

- PK: tất cả bảng có PK (`UUID`, `SERIAL`, `BIGSERIAL` tùy bảng).
- FK: khai báo rõ trong V2-V6 cho metro/collection/reward/monetization/community.
- Indexes nổi bật:
  - scan hot-path: `stations.nfc_tag_id`, `stations.qr_code_token`
  - anti-cheat/lookup: `user_stamps` indexes theo `user_id`, `collected_at`, `station_id`
  - reward allocation: `voucher_pool (reward_id, is_redeemed)`
  - ads analytics: indexes theo `(advertisement_id, impression_at)`, `(banner_id, clicked_at)`
  - referral/notification read-path: `referral_codes.code`, `notifications(user_id, is_read)`
- Unique constraints quan trọng:
  - `user_stamps`: unique `(user_id, station_id, campaign_id)` với `NULLS NOT DISTINCT`
  - `user_rewards`: unique `(user_id, milestone_id)`
  - `user_roles`: unique `(user_id, role_id)`
  - `role_permissions`: unique `(role_id, permission_id)`

---

## 3. Entities & Models

## 3.1 Entities đang có code

- User domain:
  - `User`, `UserStatus`
- Auth domain:
  - `AccessToken`, `AuditLog`, `OtpType`
- RBAC domain:
  - `Role`, `Permission`, `UserRole`, `RolePermission`, `RoleName`, `PermissionName`
- Infra:
  - `MailJob`, `MailJobStatus`, `MailContentType`

## 3.2 Entities theo schema đã thiết kế (chưa có code module)

- Metro: `Line`, `Station`
- Collection: `Campaign`, `CampaignStation`, `StampDesign`, `UserStamp`
- Reward: `Partner`, `Milestone`, `Reward`, `VoucherPool`, `UserReward`
- Monetization: `Advertisement`, `AdImpression`, `AffiliateBanner`, `AffiliateBannerClick`
- Community: `ReferralCode`, `Referral`, `ShareEvent`, `Notification`

## 3.3 Attributes & business rules nổi bật

- `User`:
  - unique: `username`, `email`, `phoneNumber`
  - domain validations: email format, username pattern, phone regex, password >= 8, age >= 6, allowed OAuth providers, status/verified consistency
  - JPA hooks: `@PrePersist/@PreUpdate` normalize + validate
- `AccessToken`:
  - lưu `tokenHash`, `expiresAt`, revoke metadata, device fingerprint
  - rule: `isValid()` = not revoked + not expired
- `UserRole`:
  - `user_id` UUID (không map ManyToOne user để tránh cross-module coupling)
- `MailJob`:
  - retry policy fields (`retryCount`, `maxRetries`, `nextRetryAt`), status state machine cơ bản
- Schema-level rules:
  - anti-cheat collect stamp
  - one-reward-per-milestone
  - referral no self-refer
  - enum state checks bằng CHECK constraints

---

## 4. Modules & Features

## 4.1 Features chính hiện tại

- Authentication & authorization:
  - register/login/refresh/logout/logout-all
  - verify email
  - forgot password + resend OTP + reset password
- User management cơ bản: CRUD + `/me`
- RBAC: assign/revoke role, query role/permission
- Infra mail queue + Redis caching/token support

## 4.2 Module ưu tiên triển khai tiếp

1. `metro` (line/station lookup, NFC/QR scan base)
2. `collection` (collect stamp + stamp book)
3. `reward` (milestone evaluate + voucher issuing)
4. `monetization` (ads/affiliate tracking)
5. `community` (referral/share/notification)

## 4.3 Dependencies giữa modules

- `auth` phụ thuộc `user` + `rbac` (login/register + roles in token)
- `rbac` tách tương đối độc lập, tham chiếu user qua UUID
- `collection` phụ thuộc `metro`
- `reward` phụ thuộc `collection` (+ `metro`/`campaign` context)
- `monetization` phụ thuộc `metro`/`campaign`/`partner`
- `community` phụ thuộc `user` + `collection`

---

## 5. API Endpoints

## 5.1 REST endpoints hiện có

### Auth (`/api/v1/auth`)

- `POST /login`
- `POST /register`
- `POST /verify-email`
- `POST /resend-verification`
- `POST /forgot-password`
- `POST /resend-otp`
- `POST /reset-password`
- `POST /refresh`
- `POST /logout`
- `POST /logout-all`

### User (`/api/v1/users`)

- `GET /{id}`
- `GET /me`
- `POST /`
- `PUT /{id}`
- `DELETE /{id}`

### RBAC (`/api/v1/roles`)

- `GET /`
- `GET /{userId}/roles`
- `POST /assign`
- `POST /revoke`
- `GET /{roleId}/permissions`

## 5.2 Request/Response structures (rút gọn)

- Auth requests:
  - `LoginRequest { identifier, password, deviceFingerprint? }`
  - `RegisterRequest { firstname, lastname, username, email, phoneNumber, password }`
  - `ForgotPasswordRequest { email }`
  - `ResendOtpRequest { email }`
  - `ResetPasswordRequest { email, otp(6), newPassword(min8) }`
  - `VerifyTokenRequest { token }`
- Auth response:
  - `AuthResponse { accessToken, tokenType, userInfo{id,email,username,roles} }`
  - refresh token trả qua HttpOnly cookie
- User response:
  - `UserResponse { id, firstname, lastname, username, email, phoneNumber, dob, gender, bio, avatarUrl, status, created_at }`
- RBAC response:
  - `ApiResponse<List<RoleResponse>>`, `ApiResponse<List<PermissionResponse>>`

## 5.3 Authentication & authorization

- `SecurityConfig` cho phép public:
  - login/register/refresh/verify-email/forgot-password/resend-otp/reset-password/resend-verification
  - swagger endpoints
- Các endpoint khác cần JWT.
- RBAC admin-only với `@PreAuthorize("hasRole('ADMIN')")` trên RoleController.

---

## 6. Current Code Status

## 6.1 Đã viết

- `auth`: 47 files
- `user`: 23 files
- `rbac`: 24 files
- shared infra/config/common đã khá đầy đủ

## 6.2 Chưa hoàn thành / chưa bắt đầu code business

- `modules/metro`: 0 files
- `modules/collection`: 0 files
- `modules/reward`: 0 files
- `modules/monetization`: 0 files
- `modules/community`: 0 files

## 6.3 Partial/skeleton hiện có

- Các package rỗng trong module đã có code:
  - `auth/application/query`
  - `auth/domain/event`
  - `auth/domain/service`
  - `auth/infrastructure/cache`
  - `rbac/application/query`
  - `rbac/domain/event`
- `integration/*` có cấu trúc thư mục nhưng chưa có implementation file.

---

## 7. Dependencies & Libraries

## 7.1 npm packages?

- Không dùng npm cho backend này (không phải Node.js project).

## 7.2 ORM/Query builder

- ORM: **Spring Data JPA + Hibernate**.
- Không dùng TypeORM/Prisma/Sequelize.

## 7.3 Validation library

- **Jakarta Bean Validation** (Hibernate Validator) qua `spring-boot-starter-validation`.

## 7.4 Testing framework

- JUnit 5 (qua spring-boot-starter-test)
- Mockito (`mockito-core`, `mockito-junit-jupiter`)
- Spring Security Test
- JavaFaker (test data)

---

## 8. Conventions & Standards

## 8.1 Naming conventions

- Java code: `camelCase` cho fields/methods, `PascalCase` cho class.
- DB schema: `snake_case` cho table/column names.
- Pattern naming:
  - `{Entity}CommandService`, `{Entity}QueryService`
  - `Jpa{Entity}Repository`
  - `{Entity}RepositoryAdapter`
  - `{Action}{Entity}Request`, `{Entity}Response`

## 8.2 Coding style & patterns

- Spring-pragmatic DDD theo module/layer.
- CQRS-lite (command/query split).
- Repository adapter pattern để tách domain khỏi JPA.
- Cache-aside pattern cho read-heavy paths.
- Mail async bằng queue + worker/retry.

## 8.3 Error handling approach

- Centralized handler: `GlobalExceptionHandler` (`@RestControllerAdvice`).
- Trả chuẩn `ErrorResponse { code, message, status, path, timestamp }`.
- Mapping domain/security/validation exceptions sang HTTP status phù hợp (400/401/403/404/409/422/429/500).
- Có unwrap root cause cho DB/rollback errors và parse duplicate key để trả message rõ field conflict.

---

## Ghi chú

- `application.yml` hiện để `jwt.issuer: metricsX` (legacy naming), có thể cần đổi sang issuer theo brand Exotic Stamp khi chốt release.
- Phần schema V2-V6 đã sẵn sàng cho roadmap Metro/Collection/Reward/Monetization/Community nhưng code module tương ứng chưa được implement.
