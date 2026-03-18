package com.metrostamp.api.modules.auth.dto;

public record LoginRequest(
        String email,
        String password
) {
}

