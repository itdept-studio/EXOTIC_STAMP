# Hướng Dẫn Test Endpoint User, Auth, RBAC (Swagger/Postman)

## 1. Mục tiêu

Tài liệu này giúp bạn test đầy đủ endpoint của 3 module:
- `auth`
- `user`
- `rbac`

Trọng tâm:
- Test luồng xác thực: `login/register/verify/resend/refresh/logout/logout-all/reset-password`
- Test edge case revoke token cũ (deny list theo `jti`) và `token_version`
- Hiểu rõ flow RBAC và cách test đúng bằng Swagger/Postman

## 2. Phạm vi endpoint cần test

### 2.1 Auth (`/api/v1/auth`)
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

### 2.2 User (`/api/v1/users`)
- `GET /{id}`
- `GET /me`
- `POST /`
- `PUT /{id}`
- `DELETE /{id}`

### 2.3 RBAC (`/api/v1/roles`)
Các endpoint RBAC hiện tại yêu cầu `hasRole('ADMIN')`:
- `GET /`
- `GET /{userId}/roles`
- `POST /assign`
- `POST /revoke`
- `GET /{roleId}/permissions`

## 3. Chuẩn bị môi trường test

1. Chạy PostgreSQL + Redis.
2. Chạy ứng dụng backend.
3. Mở Swagger: `http://localhost:8080/swagger-ui/index.html`

Tài khoản admin seed mặc định:
- `identifier`: `admin` (hoặc email `admin@exoticstamp.local`)
- `password`: `f123`

## 4. Cách test bằng Swagger

## 4.1 Auth token trên Swagger

Với endpoint cần `Authorization: Bearer <token>`:
1. Gọi `POST /api/v1/auth/login`.
2. Copy `accessToken` từ response.
3. Bấm nút **Authorize** trên Swagger.
4. Nhập `Bearer <accessToken>`.

Lưu ý:
- `refresh_token` đang lưu bằng cookie, Swagger UI có thể không ổn định khi giữ cookie giữa các request tùy trình duyệt/CORS.
- Với flow cần cookie (`/auth/refresh`), Postman sẽ dễ test hơn.

## 4.2 Test nhanh bằng Swagger (không cần cookie)

1. `POST /auth/login` → mong đợi `200`.
2. `GET /users/me` với Bearer token → mong đợi `200`.
3. `POST /auth/logout-all` với Bearer token → mong đợi `200`.
4. Gọi lại `GET /users/me` bằng access token cũ → mong đợi `401` (do `token_version` đã tăng).

## 5. Cách test bằng Postman (khuyến nghị cho auth đầy đủ)

## 5.1 Tạo collection + biến môi trường

Tạo các biến:
- `baseUrl` = `http://localhost:8080`
- `accessToken`
- `userId`
- `roleId`

## 5.2 Cấu hình tự động lưu access token sau login

Trong request `POST /auth/login`, tab **Tests**:

```javascript
const json = pm.response.json();
pm.environment.set("accessToken", json.accessToken);
```

Với các request cần auth, đặt Authorization kiểu **Bearer Token**:
- `{{accessToken}}`

## 5.3 Cookie refresh token

Sau `POST /auth/login`, Postman tự giữ cookie `refresh_token` theo domain.
Bạn kiểm tra ở **Cookies** của request.

## 6. Kịch bản test Auth chi tiết

## 6.1 Login

Request:
- `POST {{baseUrl}}/api/v1/auth/login`

Body mẫu:

```json
{
  "identifier": "admin",
  "password": "f123"
}
```

Mong đợi:
- `200`
- Response có `accessToken`
- Cookie `refresh_token` được set

## 6.2 Refresh token

Request:
- `POST {{baseUrl}}/api/v1/auth/refresh`
- Không cần body
- Phải có cookie `refresh_token`

Mong đợi:
- `200`
- Access token mới
- Cookie refresh token mới (rotate)

## 6.3 Logout 1 thiết bị (revoke access token hiện tại)

Request:
- `POST {{baseUrl}}/api/v1/auth/logout`
- Bearer token: `{{accessToken}}`

Mong đợi:
- `200`
- Dùng lại chính access token vừa logout để gọi `GET /users/me` sẽ bị `401`

Ý nghĩa:
- Access token cũ bị đưa vào deny list (theo `jti`)

## 6.4 Logout tất cả thiết bị (`token_version`)

Request:
- `POST {{baseUrl}}/api/v1/auth/logout-all`
- Bearer token: token đang còn hạn

Mong đợi:
- `200`
- Access token cũ không dùng lại được (`401`)

Ý nghĩa:
- Hệ thống tăng `users.token_version`, token cũ mismatch phiên bản

## 6.5 Register + verify email

1. `POST /auth/register` với email mới → `200`.
2. Lấy token verify từ mail (hoặc nguồn test nội bộ của bạn).
3. `POST /auth/verify-email`:

```json
{
  "token": "verify-token-here"
}
```

Mong đợi: `200`.

## 6.6 Resend verification

Request:
- `POST /auth/resend-verification`

```json
{
  "email": "newuser@example.com"
}
```

Mong đợi:
- Bình thường: `200`
- Gửi quá nhanh: `429 RESEND_COOLDOWN`
- User đã active: `422 DOMAIN_RULE_VIOLATION`

## 6.7 Forgot password / resend OTP / reset password

1. `POST /auth/forgot-password`:

```json
{
  "email": "newuser@example.com"
}
```

Mong đợi: luôn `200` (chống lộ user tồn tại/không tồn tại).

2. `POST /auth/resend-otp`:

```json
{
  "email": "newuser@example.com"
}
```

Mong đợi:
- `200` hoặc `429` (cooldown/max attempts)

3. `POST /auth/reset-password`:

```json
{
  "email": "newuser@example.com",
  "otp": "123456",
  "newPassword": "newStrongPassword123"
}
```

Mong đợi:
- OTP đúng: `200`
- OTP sai: `400 OTP_INVALID`
- OTP hết hạn: `400 OTP_EXPIRED`

Sau reset password:
- Các refresh token cũ bị revoke
- `token_version` tăng, access token cũ bị vô hiệu

## 7. Kịch bản test User module

Với Bearer token hợp lệ:

1. `GET /users/me` → `200`
2. `POST /users` với body hợp lệ → `201`
3. `GET /users/{id}` → `200`
4. `PUT /users/{id}` → `200`
5. `DELETE /users/{id}` → `204`
6. Gọi lại `GET /users/{id}` → `404 USER_NOT_FOUND`

Validation nên test thêm:
- Email sai format → `400`
- `dob` ở tương lai → `400`
- Trùng `email/username/phone` → `409`

## 8. Flow RBAC hiện tại (rất quan trọng)

Khi request vào endpoint protected:
1. `JwtAuthFilter` parse access token.
2. Validator kiểm tra:
- deny list theo `jti`
- `tokenVersion` (Redis cache, fallback DB)
3. Nếu token hợp lệ, hệ thống load role từ DB (`RoleQueryService.getRoleNamesByUserId`).
4. Role được map thành authority dạng `ROLE_<ROLE_NAME>`.
5. `@PreAuthorize("hasRole('ADMIN')")` kiểm tra quyền.

Điểm cần nhớ:
- Quyền thực thi không phụ thuộc claim `roles` trong JWT để authorize.
- Vì role load từ DB theo mỗi request, vừa assign/revoke role là có hiệu lực ngay cả với access token cũ còn hạn.

## 9. Cách test RBAC bằng Postman/Swagger

## 9.1 Chuẩn bị

Tạo 2 user:
- User A: ADMIN (admin seed)
- User B: user thường

## 9.2 Kịch bản chuẩn để hiểu flow

1. Login User B, gọi `GET /api/v1/roles`:
- Mong đợi `403 ACCESS_DENIED`

2. Login User A, gọi `POST /api/v1/roles/assign`:

```json
{
  "userId": "<UUID_USER_B>",
  "roleName": "ADMIN"
}
```

3. Dùng lại token cũ của User B, gọi `GET /api/v1/roles`:
- Mong đợi `200`

4. User A gọi `POST /api/v1/roles/revoke` với cùng payload:

```json
{
  "userId": "<UUID_USER_B>",
  "roleName": "ADMIN"
}
```

5. Dùng lại token cũ của User B, gọi `GET /api/v1/roles`:
- Mong đợi `403`

Nếu đúng như trên, bạn đã xác nhận đúng flow RBAC hiện tại.

## 10. Checklist mã trạng thái cần đạt

## 10.1 Auth
- Login thành công: `200`
- Thiếu/sai credentials: `401`
- Refresh thiếu cookie: `401 INVALID_TOKEN`
- Logout: `200`
- Logout-all: `200`
- OTP sai/hết hạn: `400`
- Resend quá nhanh: `429`

## 10.2 User
- Chưa auth: `401`
- Input không hợp lệ: `400`
- Trùng dữ liệu unique: `409`
- Không tìm thấy user: `404`

## 10.3 RBAC
- Không token: `401`
- Có token nhưng không đủ quyền ADMIN: `403`
- Có quyền ADMIN: `200`
- Assign role bị trùng: `409 ROLE_ALREADY_ASSIGNED`
- Role không tồn tại: `404 ROLE_NOT_FOUND`

## 11. Lỗi hay gặp khi test Swagger/Postman

1. Quên thêm `Bearer ` trước token trong Authorization header.
2. Test `/auth/refresh` nhưng cookie `refresh_token` không được gửi kèm.
3. Nhầm `401` và `403`:
- `401`: chưa xác thực / token lỗi / token hết hạn
- `403`: đã xác thực nhưng thiếu quyền

## 12. Gợi ý mở rộng test tự động

Nên bổ sung integration test cho:
- Auth: login/refresh/logout/logout-all/reset-password/reuse attack
- RBAC: assign/revoke role và hiệu lực tức thì
- User: CRUD + validation + duplicate data
