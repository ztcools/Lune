package com.lune.service;

import com.lune.common.Result;
import com.lune.dto.LoginRequest;
import com.lune.dto.LoginResponse;
import com.lune.dto.RegisterRequest;

public interface AuthService {
    Result<LoginResponse> login(LoginRequest request);
    Result<Void> register(RegisterRequest request);
    Result<Void> logout(String token);
}
