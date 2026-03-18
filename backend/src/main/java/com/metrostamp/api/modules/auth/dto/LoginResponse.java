package com.metrostamp.api.modules.auth.dto;

public record LoginResponse(
        String email,
        String accessToken,
        String refreshToken
) {
}

