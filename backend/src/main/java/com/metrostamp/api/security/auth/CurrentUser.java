package com.metrostamp.api.security.auth;

public record CurrentUser(
        Long id,
        String email
) {
}

