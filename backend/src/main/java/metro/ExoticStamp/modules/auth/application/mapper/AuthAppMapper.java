package metro.ExoticStamp.modules.auth.application.mapper;

import metro.ExoticStamp.modules.auth.application.command.ForgotPasswordCommand;
import metro.ExoticStamp.modules.auth.application.command.LoginCommand;
import metro.ExoticStamp.modules.auth.application.command.RegisterCommand;
import metro.ExoticStamp.modules.auth.application.command.ResetPasswordCommand;
import metro.ExoticStamp.modules.auth.presentation.dto.request.ForgotPasswordRequest;
import metro.ExoticStamp.modules.auth.presentation.dto.request.LoginRequest;
import metro.ExoticStamp.modules.auth.presentation.dto.request.RegisterRequest;
import metro.ExoticStamp.modules.auth.presentation.dto.request.ResetPasswordRequest;

public final class AuthAppMapper {

    private AuthAppMapper() {
        // prevent instantiation
    }

    public static LoginCommand toLoginCommand(LoginRequest req, String ip, String userAgent) {
        return LoginCommand.builder()
                .identifier(req.getIdentifier())
                .password(req.getPassword())
                .ipAddress(ip)
                .userAgent(userAgent)
                .deviceFingerprint(req.getDeviceFingerprint())
                .build();
    }

    public static RegisterCommand toRegisterCommand(RegisterRequest req) {
        return RegisterCommand.builder()
                .firstname(req.getFirstname())
                .lastname(req.getLastname())
                .username(req.getUsername())
                .email(req.getEmail())
                .phoneNumber(req.getPhoneNumber())
                .password(req.getPassword())
                .build();
    }

    public static ForgotPasswordCommand toForgotPasswordCommand(ForgotPasswordRequest req) {
        return ForgotPasswordCommand.builder()
                .email(req.getEmail())
                .build();
    }

    public static ResetPasswordCommand toResetPasswordCommand(ResetPasswordRequest req) {
        return ResetPasswordCommand.builder()
                .email(req.getEmail())
                .otp(req.getOtp())
                .newPassword(req.getNewPassword())
                .build();
    }
}

