package com.metrostamp.api.modules.auth.service;

import com.metrostamp.api.modules.auth.dto.LoginRequest;
import com.metrostamp.api.modules.auth.dto.LoginResponse;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public LoginResponse login(LoginRequest request) {
        return new LoginResponse(
                request.email(),
                "placeholder-access-token",
                "placeholder-refresh-token"
        );
    }
}

