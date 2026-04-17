# Auth API Integration Guide (for Flutter Team)

This document describes the Authentication APIs used by the mobile app UI.
It is based on the current backend implementation in `AuthController` and related services.

## 1. Overview

- Base path: `/api/v1/auth`
- Auth style:
  - `accessToken` is returned in response body (JWT, short-lived).
  - `refreshToken` is stored in an `HttpOnly` cookie named `refresh_token` (7 days).
- Access token TTL: `15 minutes`
- Refresh token TTL: `7 days`
- Refresh token cookie:
  - Name: `refresh_token`
  - Path: `/api/v1/auth/refresh`
  - HttpOnly: `true`
  - Max-Age: `604800` seconds
- Protected endpoints require header:
  - `Authorization: Bearer <accessToken>`

## 2. Response Conventions

### 2.1 Success Response Patterns

There are 3 success patterns:

1. Plain JSON object (example: login/refresh):
```json
{
  "accessToken": "eyJ...",
  "tokenType": "Bearer",
  "userInfo": {
    "id": "3fce6a4d-9a19-42f0-9df2-43f43dc1c362",
    "email": "user@example.com",
    "username": "user01",
    "roles": ["USER"]
  }
}
```

2. Standard wrapper `ApiResponse<T>` (example: verify/resend):
```json
{
  "success": true,
  "message": "Email verified successfully.",
  "data": null,
  "timestamp": "2026-04-13T10:12:30.512"
}
```

3. Empty body + HTTP 200 (example: forgot/reset/logout):
- Status `200 OK`, no JSON body.

### 2.2 Error Response Format

All errors are returned as:

```json
{
  "code": "INVALID_INPUT",
  "message": "email: must be a well-formed email address",
  "status": 400,
  "path": "/api/v1/auth/forgot-password",
  "timestamp": "2026-04-13T10:15:42.021"
}
```

## 3. Endpoint Details

## 3.1 Register

- Endpoint: `POST /api/v1/auth/register`
- Auth required: No
- Purpose: Create a new account in `PENDING_VERIFIED` state and trigger verification email event.

Request body:
```json
{
  "firstname": "John",
  "lastname": "Doe",
  "username": "john_doe",
  "email": "john@example.com",
  "phoneNumber": "+84901234567",
  "password": "StrongPass123"
}
```

Validation:
- `firstname`: required, max 50
- `lastname`: required, max 50
- `username`: required
- `email`: required, valid email format
- `phoneNumber`: required
- `password`: required, min 8

Success:
- `200 OK`
- Body (plain string):  
  `Registered successfully! Please check your email for verification.`

Possible errors:
- `400 INVALID_INPUT`
- `409 USER_TAKEN` / `EMAIL_TAKEN` / `USERNAME_TAKEN` / `PHONE_NUMBER_TAKEN`

UI notes:
- After success, navigate to "Check your email" screen.
- Account cannot login until verified (`USER_NOT_ACTIVE` on login).

## 3.2 Verify Email

- Endpoint: `POST /api/v1/auth/verify-email`
- Auth required: No
- Purpose: Activate account using verification token from email link.

Request body:
```json
{
  "token": "uuid-or-random-token"
}
```

Success:
- `200 OK`
```json
{
  "success": true,
  "message": "Email verified successfully.",
  "data": null,
  "timestamp": "2026-04-13T10:20:00.000"
}
```

Possible errors:
- `400 INVALID_INPUT`
- `401 INVALID_TOKEN` (invalid/expired token)

UI notes:
- On success, show "Email verified" and route user to login.

## 3.3 Resend Verification Email

- Endpoint: `POST /api/v1/auth/resend-verification`
- Auth required: No
- Purpose: Resend account verification email.

Request body:
```json
{
  "email": "john@example.com"
}
```

Success:
- `200 OK`
```json
{
  "success": true,
  "message": "Verification email sent. Please check your inbox.",
  "data": null,
  "timestamp": "2026-04-13T10:21:00.000"
}
```

Possible errors:
- `400 INVALID_INPUT`
- `404 USER_NOT_FOUND`
- `422 DOMAIN_RULE_VIOLATION` (already verified)
- `429 RESEND_COOLDOWN` (must wait, currently cooldown ~2 minutes)

UI notes:
- If `RESEND_COOLDOWN`, parse remaining seconds from `message` and show countdown.

## 3.4 Login

- Endpoint: `POST /api/v1/auth/login`
- Auth required: No
- Purpose: Authenticate user, return access token, set refresh cookie.

Request body:
```json
{
  "identifier": "john@example.com",
  "password": "StrongPass123",
  "deviceFingerprint": "optional-device-id"
}
```

Field notes:
- `identifier` accepts email OR username.
- `deviceFingerprint` is optional but recommended (stable device id).

Success:
- `200 OK`
- Header includes `Set-Cookie: refresh_token=...; HttpOnly; Path=/api/v1/auth/refresh; Max-Age=604800`
- Body:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "userInfo": {
    "id": "3fce6a4d-9a19-42f0-9df2-43f43dc1c362",
    "email": "john@example.com",
    "username": "john_doe",
    "roles": ["USER"]
  }
}
```

Possible errors:
- `400 INVALID_INPUT`
- `401 INVALID_CREDENTIALS`
- `403 USER_NOT_ACTIVE`

UI notes:
- Save `accessToken` in secure local storage.
- Ensure HTTP client persists cookies for refresh flow.

## 3.5 Refresh Access Token

- Endpoint: `POST /api/v1/auth/refresh`
- Auth required: No (uses refresh cookie)
- Purpose: Rotate refresh token and issue new access token.

Request body:
- None

Required:
- Valid `refresh_token` cookie must be sent.

Success:
- `200 OK`
- Returns new access token.
- Also returns new `Set-Cookie` with rotated refresh token.
```json
{
  "accessToken": "new-access-token",
  "tokenType": "Bearer",
  "userInfo": {
    "id": "3fce6a4d-9a19-42f0-9df2-43f43dc1c362",
    "email": "john@example.com",
    "username": "john_doe",
    "roles": ["USER"]
  }
}
```

Possible errors:
- `401 INVALID_TOKEN` (missing/invalid refresh token)
- `401 SECURITY_BREACH` (refresh token reuse detected; all sessions revoked)

UI notes:
- If refresh fails, force logout and clear local auth state.
- Refresh token is cookie-only; backend does not expose it in JSON body.

## 3.6 Logout Current Session

- Endpoint: `POST /api/v1/auth/logout`
- Auth required: Yes (`Authorization: Bearer ...`)
- Purpose: Logout current session, revoke refresh token (if cookie exists), and invalidate current access token.

Request body:
- None

Success:
- `200 OK` with empty body.
- Clears refresh cookie.

Possible errors:
- `401 UNAUTHORIZED` / `401 INVALID_TOKEN`

UI notes:
- Always clear local `accessToken` after successful logout.

## 3.7 Logout All Sessions

- Endpoint: `POST /api/v1/auth/logout-all`
- Auth required: Yes
- Purpose: Revoke all refresh tokens and invalidate all active access tokens for the user.

Request body:
- None

Success:
- `200 OK` with empty body.
- Clears refresh cookie for current client.

Possible errors:
- `401 UNAUTHORIZED` / `401 INVALID_TOKEN`

UI notes:
- Use this for "Sign out from all devices".

## 3.8 Forgot Password (Start Flow)

- Endpoint: `POST /api/v1/auth/forgot-password`
- Auth required: No
- Purpose: Send OTP for password reset (if account exists).

Request body:
```json
{
  "email": "john@example.com"
}
```

Success:
- `200 OK` with empty body in all cases (including unknown email).

Possible errors:
- `400 INVALID_INPUT`

UI notes:
- Backend intentionally prevents user enumeration.
- UI should always show neutral message, e.g. "If your email exists, OTP has been sent."

## 3.9 Resend OTP (Forgot Password)

- Endpoint: `POST /api/v1/auth/resend-otp`
- Auth required: No
- Purpose: Resend forgot-password OTP with cooldown/attempt guard.

Request body:
```json
{
  "email": "john@example.com"
}
```

Success:
- `200 OK`
```json
{
  "success": true,
  "message": "If the email exists, a new OTP has been sent.",
  "data": null,
  "timestamp": "2026-04-13T10:30:00.000"
}
```

Possible errors:
- `400 INVALID_INPUT`
- `429 RESEND_COOLDOWN` (cooldown ~2 minutes)
- `429 OTP_MAX_ATTEMPTS_EXCEEDED` (max 5 resend attempts in rolling ~1 hour window)

UI notes:
- Show resend countdown timer.
- Disable resend button during cooldown.

## 3.10 Reset Password

- Endpoint: `POST /api/v1/auth/reset-password`
- Auth required: No
- Purpose: Validate OTP, update password, revoke all sessions.

Request body:
```json
{
  "email": "john@example.com",
  "otp": "123456",
  "newPassword": "NewStrongPass123"
}
```

Validation:
- `email`: required, valid email
- `otp`: required, exactly 6 chars
- `newPassword`: required, min 8

Success:
- `200 OK` with empty body.

Possible errors:
- `400 INVALID_INPUT`
- `400 OTP_EXPIRED`
- `400 OTP_INVALID`
- `404 USER_NOT_FOUND` (edge case if email disappears between steps)

Security behavior after success:
- Revokes all refresh tokens for user.
- Invalidates active access tokens server-side.

UI notes:
- After success, navigate to login and clear any local auth state.

## 4. Auth Workflow for UI

## 4.1 Registration and Verification
1. Call `register`.
2. Show "check email" screen.
3. User opens email link and app/web gets token.
4. Call `verify-email`.
5. Route to login.

## 4.2 Login and Session Lifecycle
1. Call `login`.
2. Save `accessToken`; keep cookie jar for `refresh_token`.
3. Call protected APIs with `Authorization: Bearer <accessToken>`.
4. If API returns `401` due token issues, call `refresh`.
5. If refresh success, retry original API once.
6. If refresh fails, force logout to login screen.

## 4.3 Forgot Password
1. Call `forgot-password`.
2. User inputs OTP from email.
3. If needed call `resend-otp` (respect cooldown/errors).
4. Call `reset-password`.
5. Redirect to login.

## 5. Flutter Integration Notes

- Use an HTTP client with cookie persistence (for refresh cookie), for example:
  - `dio` + `cookie_jar` + `dio_cookie_manager`
- Always send:
  - `Content-Type: application/json`
- For protected endpoints, send:
  - `Authorization: Bearer <accessToken>`
- Implement a centralized interceptor:
  - On `401` from business API, try one `refresh` call.
  - Prevent parallel refresh storms (single-flight refresh lock).
  - Retry original request once after successful refresh.
- Store `accessToken` securely (`flutter_secure_storage` recommended).
- Do not try to read `refresh_token` from JS/Dart directly (HttpOnly by design).

## 6. Quick Error Code Reference (Auth)

- `INVALID_INPUT` (400)
- `INVALID_CREDENTIALS` (401)
- `UNAUTHORIZED` (401)
- `INVALID_TOKEN` (401)
- `TOKEN_EXPIRED` (401)
- `SECURITY_BREACH` (401)
- `USER_NOT_ACTIVE` (403)
- `ACCESS_DENIED` (403)
- `USER_NOT_FOUND` (404)
- `USER_TAKEN` / `EMAIL_TAKEN` / `USERNAME_TAKEN` / `PHONE_NUMBER_TAKEN` (409)
- `RESEND_COOLDOWN` (429)
- `OTP_MAX_ATTEMPTS_EXCEEDED` (429)
- `DOMAIN_RULE_VIOLATION` (422)

## 7. Postman/cURL Examples

Login:
```bash
curl -X POST "{{baseUrl}}/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "identifier":"john@example.com",
    "password":"StrongPass123",
    "deviceFingerprint":"device-abc-123"
  }' \
  -c cookies.txt
```

Refresh:
```bash
curl -X POST "{{baseUrl}}/api/v1/auth/refresh" \
  -b cookies.txt \
  -c cookies.txt
```

Logout:
```bash
curl -X POST "{{baseUrl}}/api/v1/auth/logout" \
  -H "Authorization: Bearer {{accessToken}}" \
  -b cookies.txt \
  -c cookies.txt
```
