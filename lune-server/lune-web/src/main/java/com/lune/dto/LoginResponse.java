package com.lune.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String token;
    private Long userId;
    private String username;
    private String nickname;
    private String email;
    private String avatar;
    private String gender;
    private String signature;
    private String role;
}
