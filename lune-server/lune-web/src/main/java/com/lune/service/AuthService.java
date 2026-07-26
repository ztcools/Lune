package com.lune.service;

import com.lune.common.Result;
import com.lune.dto.*;

public interface AuthService {
    Result<LoginResponse> login(LoginRequest request);
    Result<LoginResponse> register(RegisterRequest request);
    Result<Void> sendCode(SendCodeRequest request);
    Result<Void> logout(String token);
}
