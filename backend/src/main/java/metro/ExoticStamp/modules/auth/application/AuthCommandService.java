package metro.ExoticStamp.modules.auth.application;

import metro.ExoticStamp.common.exceptions.DomainRuleViolationException;
import metro.ExoticStamp.infra.mail.MailService;
import metro.ExoticStamp.modules.auth.application.command.ForgotPasswordCommand;
import metro.ExoticStamp.modules.auth.application.command.LoginCommand;
import metro.ExoticStamp.modules.auth.application.command.RefreshTokenCommand;
import metro.ExoticStamp.modules.auth.application.command.RegisterCommand;
import metro.ExoticStamp.modules.auth.application.command.ResendOtpCommand;
import metro.ExoticStamp.modules.auth.application.command.ResendVerificationCommand;
import metro.ExoticStamp.modules.auth.application.command.ResetPasswordCommand;
import metro.ExoticStamp.modules.auth.application.command.VerifyEmailOtpCommand;
import metro.ExoticStamp.modules.auth.domain.exception.InvalidCredentialsException;
import metro.ExoticStamp.modules.auth.domain.exception.InvalidTokenException;
import metro.ExoticStamp.modules.auth.domain.exception.OtpMaxAttemptsExceededException;
import metro.ExoticStamp.modules.auth.domain.exception.OtpExpiredException;
import metro.ExoticStamp.modules.auth.domain.exception.OtpInvalidException;
import metro.ExoticStamp.modules.auth.domain.exception.ResendCooldownException;
import metro.ExoticStamp.modules.auth.domain.exception.SecurityBreachException;
import metro.ExoticStamp.modules.auth.domain.exception.UserNotActiveException;
import metro.ExoticStamp.modules.auth.domain.model.AccessToken;
import metro.ExoticStamp.modules.auth.domain.model.OtpType;
import metro.ExoticStamp.modules.auth.domain.repository.AccessTokenRepository;
import metro.ExoticStamp.modules.auth.infrastructure.jwt.IssuedAccessToken;
import metro.ExoticStamp.modules.auth.infrastructure.jwt.JwtProvider;
import metro.ExoticStamp.modules.auth.infrastructure.jwt.JwtProperties;
import metro.ExoticStamp.modules.auth.infrastructure.redis.AccessTokenRevocationRedisRepository;
import metro.ExoticStamp.modules.auth.infrastructure.redis.OtpRepository;
import metro.ExoticStamp.modules.auth.infrastructure.redis.RefreshTokenRedisRepository;
import metro.ExoticStamp.modules.rbac.application.RoleQueryService;
import metro.ExoticStamp.modules.user.domain.exception.UserNotFoundException;
import metro.ExoticStamp.modules.user.domain.model.User;
import metro.ExoticStamp.modules.user.domain.model.UserStatus;
import metro.ExoticStamp.modules.user.domain.repository.UserRepository;
import metro.ExoticStamp.modules.user.domain.exception.UserFieldAlreadyTakenException;
import metro.ExoticStamp.modules.auth.presentation.dto.response.AuthResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.security.SecureRandom;
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthCommandService {

    private static final String TOKEN_TYPE_REFRESH = "REFRESH";
    private static final String TOKEN_PREFIX_BEARER = "Bearer";

    private static final String AUDIT_TABLE_ACCESS_TOKENS = "access_tokens";
    private static final String AUDIT_ACTION_LOGIN = "LOGIN";

    private static final int OTP_LENGTH = 6;
    private static final String OTP_CHARS = "0123456789";
    private static final SecureRandom RNG = new SecureRandom();
    private static final int OTP_MAX_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final AccessTokenRepository accessTokenRepository;
    private final RoleQueryService roleQueryService;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRedisRepository refreshTokenRedis;
    private final AccessTokenRevocationRedisRepository accessTokenRevocationRedis;
    private final OtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final JwtProperties jwtProperties;
    private final MailService mailService;

    private static final int RESEND_COOLDOWN_FALLBACK_SECONDS = 120;

    @Transactional
    public AuthResponse login(LoginCommand cmd) {
        User user = userRepository.findByEmail(cmd.getIdentifier())
                .or(() -> userRepository.findByUsername(cmd.getIdentifier()))
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(cmd.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UserNotActiveException();
        }

        List<String> roles = roleQueryService.getRoleNamesByUserId(user.getId());

        IssuedAccessToken issued = jwtProvider.issueAccessToken(user, roles);
        String accessToken = issued.token();
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());
        String tokenHash = jwtProvider.hashToken(refreshToken);

        LocalDateTime now = LocalDateTime.now();
        String deviceFingerprint = normalizeDeviceFingerprint(cmd);
        LocalDateTime expiresAt = now.plus(jwtProperties.getRefreshTokenTtl());

        AccessToken record = AccessToken.builder()
                .id(UUID.randomUUID())
                .userId(user.getId())
                .tokenHash(tokenHash)
                .tokenType(TOKEN_TYPE_REFRESH)
                .tokenPrefix(TOKEN_PREFIX_BEARER)
                .expiresAt(expiresAt)
                .ipAddress(cmd.getIpAddress())
                .userAgent(cmd.getUserAgent())
                .deviceFingerprint(deviceFingerprint)
                .createdAt(now)
                .build();

        accessTokenRepository.save(record);
        refreshTokenRedis.save(user.getId(), deviceFingerprint, tokenHash);
        accessTokenRevocationRedis.setDeviceAccessJti(user.getId(), deviceFingerprint, issued.jti());
        auditLogService.log(
                user.getId(),
                AUDIT_TABLE_ACCESS_TOKENS,
                AUDIT_ACTION_LOGIN,
                null,
                record,
                cmd.getIpAddress()
        );

        return AuthResponse.of(accessToken, user, roles, refreshToken);
    }

    @Transactional
    public void register(RegisterCommand cmd) {
        if (userRepository.existsByEmail(cmd.getEmail())) {
            throw new UserFieldAlreadyTakenException("email", cmd.getEmail());
        }
        if (userRepository.existsByUsername(cmd.getUsername())) {
            throw new UserFieldAlreadyTakenException("username", cmd.getUsername());
        }

        User user = User.builder()
                .firstname(cmd.getFirstname())
                .lastname(cmd.getLastname())
                .username(cmd.getUsername())
                .email(cmd.getEmail())
                .phoneNumber(cmd.getPhoneNumber())
                .password(passwordEncoder.encode(cmd.getPassword()))
                .status(UserStatus.PENDING_VERIFIED)
                .build();

        User saved = userRepository.save(user);

        otpRepository.delete(saved.getEmail(), OtpType.EMAIL_VERIFY);
        String otp = generateOtp();
        otpRepository.save(saved.getEmail(), OtpType.EMAIL_VERIFY, otp);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    mailService.sendEmailVerificationOtp(saved.getEmail(), otp);
                }
            });
        } else {
            mailService.sendEmailVerificationOtp(saved.getEmail(), otp);
        }
    }

    @Transactional
    public void verifyEmail(VerifyEmailOtpCommand cmd) {
        String otp = otpRepository.find(cmd.getEmail(), OtpType.EMAIL_VERIFY)
                .orElseThrow(OtpExpiredException::new);

        if (!otp.equals(cmd.getOtp())) {
            throw new OtpInvalidException();
        }

        User user = userRepository.findByEmail(cmd.getEmail())
                .orElseThrow(() -> new UserNotFoundException("email", cmd.getEmail()));

        user.setStatus(UserStatus.ACTIVE);
        user.setVerifiedAt(LocalDateTime.now());
        userRepository.save(user);

        otpRepository.delete(cmd.getEmail(), OtpType.EMAIL_VERIFY);
    }

    @Transactional
    public void resendVerification(ResendVerificationCommand cmd) {
        User user = userRepository.findByEmail(cmd.getEmail())
                .orElseThrow(() -> new UserNotFoundException("email", cmd.getEmail()));

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new DomainRuleViolationException("Account is already verified");
        }

        if (otpRepository.isOnCooldown(cmd.getEmail(), OtpType.EMAIL_VERIFY)) {
            long secondsLeft = otpRepository.getCooldownTtlSeconds(cmd.getEmail(), OtpType.EMAIL_VERIFY);
            if (secondsLeft <= 0) {
                secondsLeft = RESEND_COOLDOWN_FALLBACK_SECONDS;
            }
            throw new ResendCooldownException(secondsLeft);
        }

        if (otpRepository.isMaxAttemptsExceeded(cmd.getEmail(), OtpType.EMAIL_VERIFY)) {
            throw new OtpMaxAttemptsExceededException(OTP_MAX_ATTEMPTS);
        }

        otpRepository.delete(cmd.getEmail(), OtpType.EMAIL_VERIFY);

        String otp = generateOtp();
        otpRepository.save(cmd.getEmail(), OtpType.EMAIL_VERIFY, otp);
        otpRepository.saveCooldown(cmd.getEmail(), OtpType.EMAIL_VERIFY);
        otpRepository.incrementAttempts(cmd.getEmail(), OtpType.EMAIL_VERIFY);

        mailService.sendEmailVerificationOtp(cmd.getEmail(), otp);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordCommand cmd) {
        Optional<User> userOpt = userRepository.findByEmail(cmd.getEmail());
        if (userOpt.isEmpty()) {
            return; // prevent user enumeration
        }

        User user = userOpt.get();

        String otp = generateOtp();
        otpRepository.save(user.getEmail(), OtpType.FORGOT_PASSWORD, otp);
        mailService.sendOtpEmail(cmd.getEmail(), otp);
    }

    @Transactional
    public void resendForgotPasswordOtp(ResendOtpCommand cmd) {
        if (otpRepository.isOnCooldown(cmd.getEmail(), OtpType.FORGOT_PASSWORD)) {
            long secondsLeft = otpRepository.getCooldownTtlSeconds(cmd.getEmail(), OtpType.FORGOT_PASSWORD);
            throw new ResendCooldownException(secondsLeft);
        }

        if (otpRepository.isMaxAttemptsExceeded(cmd.getEmail(), OtpType.FORGOT_PASSWORD)) {
            throw new OtpMaxAttemptsExceededException(OTP_MAX_ATTEMPTS);
        }

        userRepository.findByEmail(cmd.getEmail()).ifPresent(user -> {
            otpRepository.delete(cmd.getEmail(), OtpType.FORGOT_PASSWORD);

            String otp = generateOtp();
            otpRepository.save(cmd.getEmail(), OtpType.FORGOT_PASSWORD, otp);

            otpRepository.saveCooldown(cmd.getEmail(), OtpType.FORGOT_PASSWORD);
            otpRepository.incrementAttempts(cmd.getEmail(), OtpType.FORGOT_PASSWORD);

            mailService.sendOtpEmail(cmd.getEmail(), otp);

            auditLogService.log(
                    user.getId(),
                    "otp",
                    "RESEND_FORGOT_PASSWORD_OTP",
                    null,
                    Map.of(
                            "email", cmd.getEmail(),
                            "attempts", otpRepository.getAttemptsCount(cmd.getEmail(), OtpType.FORGOT_PASSWORD)
                    ),
                    "SYSTEM"
            );
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordCommand cmd) {
        String otp = otpRepository.find(cmd.getEmail(), OtpType.FORGOT_PASSWORD)
                .orElseThrow(OtpExpiredException::new);

        if (!otp.equals(cmd.getOtp())) {
            throw new OtpInvalidException();
        }

        User user = userRepository.findByEmail(cmd.getEmail())
                .orElseThrow(() -> new UserNotFoundException("email", cmd.getEmail()));

        user.setPassword(passwordEncoder.encode(cmd.getNewPassword()));
        user.setPasswordUpdateAt(LocalDateTime.now());
        userRepository.save(user);

        accessTokenRepository.revokeAllByUserId(user.getId(), AccessToken.REASON_PASSWORD_RESET);
        refreshTokenRedis.revokeAllForUser(user.getId());
        bumpTokenVersionAndSyncRedis(user.getId());
        otpRepository.delete(cmd.getEmail(), OtpType.FORGOT_PASSWORD);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenCommand cmd) {
        String token = cmd.getRefreshToken();
        String tokenHash = jwtProvider.hashToken(token);

        if (refreshTokenRedis.isRevoked(tokenHash)) {
            UUID userId = jwtProvider.extractUserId(token);
            handleReuseAttack(userId);
        }

        if (!jwtProvider.isTokenValid(token)) {
            throw new InvalidTokenException("Invalid token");
        }

        UUID userId = jwtProvider.extractUserId(token);
        AccessToken record = accessTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if (!record.isValid()) {
            throw new InvalidTokenException("Refresh token is not valid");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidTokenException("User not found"));

        List<String> roles = roleQueryService.getRoleNamesByUserId(userId);

        accessTokenRevocationRedis.getDeviceAccessJti(userId, record.getDeviceFingerprint())
                .ifPresent(jti -> accessTokenRevocationRedis.addToDenylist(
                        jti,
                        jwtProperties.getAccessTokenTtl()
                ));

        IssuedAccessToken issued = jwtProvider.issueAccessToken(user, roles);
        String newAccessToken = issued.token();
        String newRefreshToken = jwtProvider.generateRefreshToken(userId);
        String newTokenHash = jwtProvider.hashToken(newRefreshToken);

        accessTokenRepository.revokeByTokenHash(tokenHash, AccessToken.REASON_ROTATED);
        refreshTokenRedis.revoke(userId, record.getDeviceFingerprint(), tokenHash);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plus(jwtProperties.getRefreshTokenTtl());

        AccessToken newRecord = AccessToken.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .tokenHash(newTokenHash)
                .tokenType(TOKEN_TYPE_REFRESH)
                .tokenPrefix(TOKEN_PREFIX_BEARER)
                .expiresAt(expiresAt)
                .ipAddress(record.getIpAddress())
                .userAgent(record.getUserAgent())
                .deviceFingerprint(record.getDeviceFingerprint())
                .createdAt(now)
                .build();

        accessTokenRepository.save(newRecord);
        refreshTokenRedis.save(userId, record.getDeviceFingerprint(), newTokenHash);
        accessTokenRevocationRedis.setDeviceAccessJti(userId, record.getDeviceFingerprint(), issued.jti());

        return AuthResponse.of(newAccessToken, user, roles, newRefreshToken);
    }

    @Transactional
    public void logout(UUID userId, Optional<String> refreshTokenOpt, Optional<String> accessJti) {
        accessJti.ifPresent(jti -> accessTokenRevocationRedis.addToDenylist(
                jti,
                jwtProperties.getAccessTokenTtl()
        ));

        if (refreshTokenOpt.isEmpty()) {
            return;
        }

        String refreshToken = refreshTokenOpt.get();
        String hash = jwtProvider.hashToken(refreshToken);
        accessTokenRepository.revokeByTokenHash(hash, AccessToken.REASON_LOGOUT);

        Optional<AccessToken> recordOpt = accessTokenRepository.findByTokenHash(hash);
        if (recordOpt.isEmpty()) {
            return;
        }

        AccessToken record = recordOpt.get();
        refreshTokenRedis.revoke(userId, record.getDeviceFingerprint(), hash);
        accessTokenRevocationRedis.deleteDeviceAccessJti(userId, record.getDeviceFingerprint());
    }

    @Transactional
    public void logoutAll(UUID userId) {
        accessTokenRepository.revokeAllByUserId(userId, AccessToken.REASON_LOGOUT_ALL);
        refreshTokenRedis.revokeAllForUser(userId);
        bumpTokenVersionAndSyncRedis(userId);
    }

    private void handleReuseAttack(UUID userId) {
        accessTokenRepository.revokeAllByUserId(userId, AccessToken.REASON_REUSE_ATTACK);
        refreshTokenRedis.revokeAllForUser(userId);
        bumpTokenVersionAndSyncRedis(userId);
        log.error("[Auth] REUSE ATTACK detected userId={}", userId);
        throw new SecurityBreachException(userId.toString());
    }

    private void bumpTokenVersionAndSyncRedis(UUID userId) {
        int updated = userRepository.incrementTokenVersionById(userId);
        if (updated == 0) {
            return;
        }
        userRepository.findTokenVersionById(userId)
                .ifPresent(v -> accessTokenRevocationRedis.setCachedTokenVersion(userId, v));
    }

    private static String generateOtp() {
        StringBuilder otp = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(OTP_CHARS.charAt(RNG.nextInt(OTP_CHARS.length())));
        }
        return otp.toString();
    }

    private String normalizeDeviceFingerprint(LoginCommand cmd) {
        String fp = cmd.getDeviceFingerprint();
        if (fp != null && !fp.isBlank()) {
            return fp;
        }
        return jwtProvider.hashToken(cmd.getUserAgent() + cmd.getIpAddress());
    }
}
