package metro.ExoticStamp.modules.auth.application;

import metro.ExoticStamp.infra.mail.MailService;
import metro.ExoticStamp.modules.auth.application.command.RegisterCommand;
import metro.ExoticStamp.modules.auth.application.command.ResendVerificationCommand;
import metro.ExoticStamp.modules.auth.application.command.VerifyEmailOtpCommand;
import metro.ExoticStamp.modules.auth.domain.exception.OtpExpiredException;
import metro.ExoticStamp.modules.auth.domain.exception.OtpInvalidException;
import metro.ExoticStamp.modules.auth.domain.exception.OtpMaxAttemptsExceededException;
import metro.ExoticStamp.modules.auth.domain.exception.ResendCooldownException;
import metro.ExoticStamp.modules.auth.domain.model.OtpType;
import metro.ExoticStamp.modules.auth.domain.repository.AccessTokenRepository;
import metro.ExoticStamp.modules.auth.infrastructure.jwt.JwtProperties;
import metro.ExoticStamp.modules.auth.infrastructure.jwt.JwtProvider;
import metro.ExoticStamp.modules.auth.infrastructure.redis.AccessTokenRevocationRedisRepository;
import metro.ExoticStamp.modules.auth.infrastructure.redis.OtpRepository;
import metro.ExoticStamp.modules.auth.infrastructure.redis.RefreshTokenRedisRepository;
import metro.ExoticStamp.modules.rbac.application.RoleQueryService;
import metro.ExoticStamp.modules.user.domain.model.User;
import metro.ExoticStamp.modules.user.domain.model.UserStatus;
import metro.ExoticStamp.modules.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthCommandServiceEmailVerificationOtpTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AccessTokenRepository accessTokenRepository;
    @Mock
    private RoleQueryService roleQueryService;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private RefreshTokenRedisRepository refreshTokenRedis;
    @Mock
    private AccessTokenRevocationRedisRepository accessTokenRevocationRedis;
    @Mock
    private OtpRepository otpRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuditLogService auditLogService;
    private final JwtProperties jwtProperties = new JwtProperties();
    @Mock
    private MailService mailService;

    private AuthCommandService service;

    @BeforeEach
    void setUp() {
        service = new AuthCommandService(
                userRepository,
                accessTokenRepository,
                roleQueryService,
                jwtProvider,
                refreshTokenRedis,
                accessTokenRevocationRedis,
                otpRepository,
                passwordEncoder,
                auditLogService,
                jwtProperties,
                mailService
        );
    }

    @Test
    void register_generatesAndSavesEmailVerifyOtp_andSendsMail() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("john_doe")).thenReturn(false);
        when(passwordEncoder.encode("StrongPass123")).thenReturn("hashed");

        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        service.register(new RegisterCommand(
                "John",
                "Doe",
                "john_doe",
                "john@example.com",
                "+84901234567",
                "StrongPass123"
        ));

        ArgumentCaptor<String> otpCaptor = ArgumentCaptor.forClass(String.class);
        verify(otpRepository).save(eq("john@example.com"), eq(OtpType.EMAIL_VERIFY), otpCaptor.capture());
        String otp = otpCaptor.getValue();
        assertNotNull(otp);
        assertEquals(6, otp.length());

        verify(mailService).sendEmailVerificationOtp(eq("john@example.com"), eq(otp));
    }

    @Test
    void verifyEmail_validOtp_activatesAccount_andDeletesOtp() {
        String email = "john@example.com";
        when(otpRepository.find(email, OtpType.EMAIL_VERIFY)).thenReturn(Optional.of("123456"));

        User user = User.builder()
                .firstname("John")
                .lastname("Doe")
                .username("john_doe")
                .email(email)
                .phoneNumber("+84901234567")
                .password("hashed")
                .status(UserStatus.PENDING_VERIFIED)
                .build();
        user.setId(UUID.randomUUID());

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        service.verifyEmail(new VerifyEmailOtpCommand(email, "123456"));

        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertNotNull(user.getVerifiedAt());
        verify(otpRepository).delete(email, OtpType.EMAIL_VERIFY);
    }

    @Test
    void verifyEmail_wrongOtp_throwsOtpInvalid() {
        String email = "john@example.com";
        when(otpRepository.find(email, OtpType.EMAIL_VERIFY)).thenReturn(Optional.of("123456"));

        assertThrows(OtpInvalidException.class, () ->
                service.verifyEmail(new VerifyEmailOtpCommand(email, "000000")));

        verify(userRepository, never()).save(any());
        verify(otpRepository, never()).delete(eq(email), eq(OtpType.EMAIL_VERIFY));
    }

    @Test
    void verifyEmail_missingOtp_throwsOtpExpired() {
        String email = "john@example.com";
        when(otpRepository.find(email, OtpType.EMAIL_VERIFY)).thenReturn(Optional.empty());

        assertThrows(OtpExpiredException.class, () ->
                service.verifyEmail(new VerifyEmailOtpCommand(email, "123456")));
    }

    @Test
    void resendVerification_onCooldown_throwsResendCooldown() {
        String email = "john@example.com";
        User user = User.builder().email(email).username("john_doe").status(UserStatus.PENDING_VERIFIED).build();
        user.setId(UUID.randomUUID());

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(otpRepository.isOnCooldown(email, OtpType.EMAIL_VERIFY)).thenReturn(true);
        when(otpRepository.getCooldownTtlSeconds(email, OtpType.EMAIL_VERIFY)).thenReturn(90L);

        assertThrows(ResendCooldownException.class, () ->
                service.resendVerification(new ResendVerificationCommand(email)));
    }

    @Test
    void resendVerification_maxAttemptsExceeded_throwsOtpMaxAttemptsExceeded() {
        String email = "john@example.com";
        User user = User.builder().email(email).username("john_doe").status(UserStatus.PENDING_VERIFIED).build();
        user.setId(UUID.randomUUID());

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(otpRepository.isOnCooldown(email, OtpType.EMAIL_VERIFY)).thenReturn(false);
        when(otpRepository.isMaxAttemptsExceeded(email, OtpType.EMAIL_VERIFY)).thenReturn(true);

        assertThrows(OtpMaxAttemptsExceededException.class, () ->
                service.resendVerification(new ResendVerificationCommand(email)));
    }

    @Test
    void resendVerification_success_sendsNewOtp_andAppliesProtections() {
        String email = "john@example.com";
        User user = User.builder().email(email).username("john_doe").status(UserStatus.PENDING_VERIFIED).build();
        user.setId(UUID.randomUUID());

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(otpRepository.isOnCooldown(email, OtpType.EMAIL_VERIFY)).thenReturn(false);
        when(otpRepository.isMaxAttemptsExceeded(email, OtpType.EMAIL_VERIFY)).thenReturn(false);

        service.resendVerification(new ResendVerificationCommand(email));

        ArgumentCaptor<String> otpCaptor = ArgumentCaptor.forClass(String.class);
        verify(otpRepository).save(eq(email), eq(OtpType.EMAIL_VERIFY), otpCaptor.capture());
        String otp = otpCaptor.getValue();
        assertNotNull(otp);
        assertEquals(6, otp.length());

        verify(otpRepository).saveCooldown(email, OtpType.EMAIL_VERIFY);
        verify(otpRepository).incrementAttempts(email, OtpType.EMAIL_VERIFY);
        verify(mailService).sendEmailVerificationOtp(email, otp);
    }
}

