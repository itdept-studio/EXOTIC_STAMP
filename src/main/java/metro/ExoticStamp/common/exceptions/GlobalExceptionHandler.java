package metro.ExoticStamp.common.exceptions;

import metro.ExoticStamp.common.response.ErrorResponse;
import metro.ExoticStamp.modules.auth.domain.exception.InvalidCredentialsException;
import metro.ExoticStamp.modules.auth.domain.exception.InvalidTokenException;
import metro.ExoticStamp.modules.auth.domain.exception.OtpExpiredException;
import metro.ExoticStamp.modules.auth.domain.exception.OtpInvalidException;
import metro.ExoticStamp.modules.auth.domain.exception.OtpMaxAttemptsExceededException;
import metro.ExoticStamp.modules.auth.domain.exception.ResendCooldownException;
import metro.ExoticStamp.modules.auth.domain.exception.SecurityBreachException;
import metro.ExoticStamp.modules.auth.domain.exception.TokenExpiredException;
import metro.ExoticStamp.modules.auth.domain.exception.UserNotActiveException;
import metro.ExoticStamp.common.exceptions.storage.FileTooLargeException;
import metro.ExoticStamp.common.exceptions.storage.InvalidFileException;
import metro.ExoticStamp.common.exceptions.storage.InvalidImageTypeException;
import metro.ExoticStamp.modules.metro.domain.exception.DuplicateNfcTagException;
import metro.ExoticStamp.modules.metro.domain.exception.DuplicateQrTokenException;
import metro.ExoticStamp.modules.metro.domain.exception.DuplicateStationCodeException;
import metro.ExoticStamp.modules.metro.domain.exception.DuplicateStationSequenceException;
import metro.ExoticStamp.modules.metro.domain.exception.LineNotFoundException;
import metro.ExoticStamp.modules.metro.domain.exception.StationInactiveException;
import metro.ExoticStamp.modules.metro.domain.exception.StationNotFoundException;
import metro.ExoticStamp.modules.rbac.domain.exception.DuplicateRbacMappingException;
import metro.ExoticStamp.modules.rbac.domain.exception.ImmutableRoleException;
import metro.ExoticStamp.modules.rbac.domain.exception.LastAdminProtectionException;
import metro.ExoticStamp.modules.rbac.domain.exception.PermissionAlreadyExistsException;
import metro.ExoticStamp.modules.rbac.domain.exception.PermissionNotFoundException;
import metro.ExoticStamp.modules.rbac.domain.exception.RoleAlreadyAssignedException;
import metro.ExoticStamp.modules.rbac.domain.exception.RoleCodeAlreadyExistsException;
import metro.ExoticStamp.modules.rbac.domain.exception.RoleNotFoundException;
import metro.ExoticStamp.modules.user.domain.exception.UserFieldAlreadyTakenException;
import metro.ExoticStamp.modules.user.domain.exception.UserNotFoundException;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.RollbackException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Pattern PG_DUP_KEY_PATTERN =
            Pattern.compile("Key \\((?<field>[^)]+)\\)=\\((?<value>[^)]+)\\) already exists\\.");

    @ExceptionHandler({UserNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(UserNotFoundException ex, HttpServletRequest req) {
        log.warn("[404] {}", ex.getMessage());
        return build(404, "USER_NOT_FOUND", ex.getMessage(), req);
    }

    @ExceptionHandler({RoleNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(RoleNotFoundException ex, HttpServletRequest req) {
        log.warn("[404] {}", ex.getMessage());
        return build(404, "ROLE_NOT_FOUND", ex.getMessage(), req);
    }

    @ExceptionHandler(PermissionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePermissionNotFound(PermissionNotFoundException ex, HttpServletRequest req) {
        log.warn("[404] {}", ex.getMessage());
        return build(404, "PERMISSION_NOT_FOUND", ex.getMessage(), req);
    }

    @ExceptionHandler(LastAdminProtectionException.class)
    public ResponseEntity<ErrorResponse> handleLastAdmin(LastAdminProtectionException ex, HttpServletRequest req) {
        log.warn("[409] {}", ex.getMessage());
        return build(409, "LAST_ADMIN_PROTECTION", ex.getMessage(), req);
    }

    @ExceptionHandler(ImmutableRoleException.class)
    public ResponseEntity<ErrorResponse> handleImmutableRole(ImmutableRoleException ex, HttpServletRequest req) {
        log.warn("[403] {}", ex.getMessage());
        return build(403, "IMMUTABLE_ROLE", ex.getMessage(), req);
    }

    @ExceptionHandler(DuplicateRbacMappingException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateMapping(DuplicateRbacMappingException ex, HttpServletRequest req) {
        log.warn("[409] {}", ex.getMessage());
        return build(409, "RBAC_DUPLICATE_MAPPING", ex.getMessage(), req);
    }

    @ExceptionHandler(PermissionAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handlePermissionExists(PermissionAlreadyExistsException ex, HttpServletRequest req) {
        log.warn("[409] {}", ex.getMessage());
        return build(409, "PERMISSION_ALREADY_EXISTS", ex.getMessage(), req);
    }

    @ExceptionHandler(RoleCodeAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleRoleCodeExists(RoleCodeAlreadyExistsException ex, HttpServletRequest req) {
        log.warn("[409] {}", ex.getMessage());
        return build(409, "ROLE_CODE_ALREADY_EXISTS", ex.getMessage(), req);
    }

    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, OptimisticLockException.class})
    public ResponseEntity<ErrorResponse> handleOptimisticLock(RuntimeException ex, HttpServletRequest req) {
        log.warn("[409] Optimistic lock: {}", ex.getMessage());
        return build(409, "CONCURRENT_MODIFICATION", "Resource was modified by another transaction", req);
    }

    @ExceptionHandler({LineNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleLineNotFound(LineNotFoundException ex, HttpServletRequest req) {
        log.warn("[404] {}", ex.getMessage());
        return build(404, "LINE_NOT_FOUND", ex.getMessage(), req);
    }

    @ExceptionHandler({StationNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleStationNotFound(StationNotFoundException ex, HttpServletRequest req) {
        log.warn("[404] {}", ex.getMessage());
        return build(404, "STATION_NOT_FOUND", ex.getMessage(), req);
    }

    @ExceptionHandler(DuplicateNfcTagException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateNfc(DuplicateNfcTagException ex, HttpServletRequest req) {
        log.warn("[409] {}", ex.getMessage());
        return build(409, "NFC_TAG_DUPLICATE", ex.getMessage(), req);
    }

    @ExceptionHandler(DuplicateQrTokenException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateQr(DuplicateQrTokenException ex, HttpServletRequest req) {
        log.warn("[409] {}", ex.getMessage());
        return build(409, "QR_TOKEN_DUPLICATE", ex.getMessage(), req);
    }

    @ExceptionHandler(DuplicateStationCodeException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateStationCode(DuplicateStationCodeException ex, HttpServletRequest req) {
        log.warn("[409] {}", ex.getMessage());
        return build(409, "STATION_CODE_DUPLICATE", ex.getMessage(), req);
    }

    @ExceptionHandler(DuplicateStationSequenceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateStationSequence(DuplicateStationSequenceException ex, HttpServletRequest req) {
        log.warn("[409] {}", ex.getMessage());
        return build(409, "STATION_SEQUENCE_DUPLICATE", ex.getMessage(), req);
    }

    @ExceptionHandler(InvalidImageTypeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidImageType(InvalidImageTypeException ex, HttpServletRequest req) {
        log.warn("[400] {}", ex.getMessage());
        return build(400, "INVALID_IMAGE_TYPE", ex.getMessage(), req);
    }

    @ExceptionHandler(FileTooLargeException.class)
    public ResponseEntity<ErrorResponse> handleFileTooLarge(FileTooLargeException ex, HttpServletRequest req) {
        log.warn("[400] {}", ex.getMessage());
        return build(400, "FILE_TOO_LARGE", ex.getMessage(), req);
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFile(InvalidFileException ex, HttpServletRequest req) {
        log.warn("[400] {}", ex.getMessage());
        return build(400, "INVALID_FILE", ex.getMessage(), req);
    }

    @ExceptionHandler(UserFieldAlreadyTakenException.class)
    public ResponseEntity<ErrorResponse> handleEmailTaken(UserFieldAlreadyTakenException ex, HttpServletRequest req) {
        log.warn("[409] {}", ex.getMessage());
        return build(409, "USER_TAKEN", ex.getMessage(), req);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest req
    ) {
        Throwable root = unwrap(ex);
        String rootMsg = root.getMessage() == null ? "" : root.getMessage();

        // Postgres duplicate key (unique constraint) -> return 409 with a clear field message
        Matcher m = PG_DUP_KEY_PATTERN.matcher(rootMsg);
        if (m.find()) {
            String field = m.group("field");
            String value = m.group("value");

            String code;
            String msg;
            if ("phone_number".equals(field)) {
                code = "PHONE_NUMBER_TAKEN";
                msg = "Phone number already taken: " + value;
            } else if ("email".equals(field)) {
                code = "EMAIL_TAKEN";
                msg = "Email already taken: " + value;
            } else if ("username".equals(field)) {
                code = "USERNAME_TAKEN";
                msg = "Username already taken: " + value;
            } else {
                code = "DUPLICATE_FIELD";
                msg = "Field already taken: " + field + "=" + value;
            }

            log.warn("[409] DataIntegrity duplicate {}={}", field, value);
            return build(409, code, msg, req);
        }

        // Fallback: still a conflict, but don't leak internal constraint details
        log.warn("[409] DataIntegrityViolation at {} {}: {}", req.getMethod(), req.getRequestURI(), rootMsg);
        return build(409, "DATA_INTEGRITY_VIOLATION", "Duplicate or conflicting data", req);
    }

    @ExceptionHandler(RoleAlreadyAssignedException.class)
    public ResponseEntity<ErrorResponse> handleEmailTaken(RoleAlreadyAssignedException ex, HttpServletRequest req) {
        log.warn("[409] {}", ex.getMessage());
        return build(409, "ROLE_ALREADY_ASSIGNED", ex.getMessage(), req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadArg(IllegalArgumentException ex, HttpServletRequest req) {
        log.warn("[400] IllegalArgument: {}", ex.getMessage());
        return build(400, "VALIDATION_ERROR", ex.getMessage(), req);
    }

    @ExceptionHandler(StationInactiveException.class)
    public ResponseEntity<ErrorResponse> handleStationInactive(StationInactiveException ex, HttpServletRequest req) {
        log.warn("[400] {}", ex.getMessage());
        return build(400, "STATION_INACTIVE", ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBeanValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("[400] BeanValidation: {}", msg);
        return build(400, "INVALID_INPUT", msg, req);
    }

    @ExceptionHandler(RollbackException.class)
    public ResponseEntity<ErrorResponse> handleRollback(RollbackException ex, HttpServletRequest req) {
        Throwable root = unwrap(ex);
        log.warn("[400] RollbackException: {}", root.getMessage());
        return build(400, "ENTITY_VALIDATION_ERROR", root.getMessage(), req);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        String msg = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(", "));
        log.warn("[400] ConstraintViolation: {}", msg);
        return build(400, "CONSTRAINT_VIOLATION", msg, req);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        log.warn("[403] AccessDenied: {} {}", req.getMethod(), req.getRequestURI());
        return build(403, "ACCESS_DENIED", "You don't have permission to access this resource", req);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuth(AuthenticationException ex, HttpServletRequest req) {
        log.warn("[401] Unauthenticated: {}", ex.getMessage());
        return build(401, "UNAUTHORIZED", "Authentication required", req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex, HttpServletRequest req) {
        log.error("[500] Unhandled at {} {}: ", req.getMethod(), req.getRequestURI(), ex);
        return build(500, "INTERNAL_ERROR", "An unexpected error occurred", req);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException ex,
            HttpServletRequest req
    ) {
        log.warn("[401] Invalid credentials: {}", ex.getMessage());
        return build(401, "INVALID_CREDENTIALS", ex.getMessage(), req);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleTokenExpired(
            TokenExpiredException ex,
            HttpServletRequest req
    ) {
        log.warn("[401] Token expired: {}", ex.getMessage());
        return build(401, "TOKEN_EXPIRED", ex.getMessage(), req);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(
            InvalidTokenException ex,
            HttpServletRequest req
    ) {
        log.warn("[401] Invalid token: {}", ex.getMessage());
        return build(401, "INVALID_TOKEN", ex.getMessage(), req);
    }

    @ExceptionHandler(OtpExpiredException.class)
    public ResponseEntity<ErrorResponse> handleOtpExpired(
            OtpExpiredException ex,
            HttpServletRequest req
    ) {
        log.warn("[400] OTP expired: {}", ex.getMessage());
        return build(400, "OTP_EXPIRED", ex.getMessage(), req);
    }

    @ExceptionHandler(OtpInvalidException.class)
    public ResponseEntity<ErrorResponse> handleOtpInvalid(
            OtpInvalidException ex,
            HttpServletRequest req
    ) {
        log.warn("[400] OTP invalid: {}", ex.getMessage());
        return build(400, "OTP_INVALID", ex.getMessage(), req);
    }

    @ExceptionHandler(UserNotActiveException.class)
    public ResponseEntity<ErrorResponse> handleUserNotActive(
            UserNotActiveException ex,
            HttpServletRequest req
    ) {
        log.warn("[403] User not active: {}", ex.getMessage());
        return build(403, "USER_NOT_ACTIVE", ex.getMessage(), req);
    }

    @ExceptionHandler(SecurityBreachException.class)
    public ResponseEntity<ErrorResponse> handleSecurityBreach(
            SecurityBreachException ex,
            HttpServletRequest req
    ) {
        log.error("[401] Security breach: {}", ex.getMessage());
        return build(401, "SECURITY_BREACH", ex.getMessage(), req);
    }

    @ExceptionHandler(ResendCooldownException.class)
    public ResponseEntity<ErrorResponse> handleResendCooldown(
            ResendCooldownException ex,
            HttpServletRequest req
    ) {
        log.warn("[429] ResendCooldown: {} seconds remaining", ex.getSecondsRemaining());
        return build(429, "RESEND_COOLDOWN", ex.getMessage(), req);
    }

    @ExceptionHandler(OtpMaxAttemptsExceededException.class)
    public ResponseEntity<ErrorResponse> handleOtpMaxAttempts(
            OtpMaxAttemptsExceededException ex, HttpServletRequest req) {
        log.warn("[429] OtpMaxAttempts: max={} at {}", ex.getMaxAttempts(), req.getRequestURI());
        return ResponseEntity.status(429)
                .body(ErrorResponse.of(
                        "OTP_MAX_ATTEMPTS_EXCEEDED",
                        ex.getMessage(),
                        429,
                        req.getRequestURI()));
    }

    private ResponseEntity<ErrorResponse> build(int status, String code, String message, HttpServletRequest req) {
        return ResponseEntity.status(status)
                .body(ErrorResponse.of(code, message, status, req.getRequestURI()));
    }

    // 422 — business rule violation
    // Khác với 400 (bad input format) và 409 (conflict/duplicate)
    @ExceptionHandler(DomainRuleViolationException.class)
    public ResponseEntity<ErrorResponse> handleDomainRule(
            DomainRuleViolationException ex, HttpServletRequest req) {
        log.warn("[422] DomainRule: {}", ex.getMessage());
        return build(422, "DOMAIN_RULE_VIOLATION", ex.getMessage(), req);
    }

    private Throwable unwrap(Throwable t) {
        return t.getCause() != null ? unwrap(t.getCause()) : t;
    }
}
