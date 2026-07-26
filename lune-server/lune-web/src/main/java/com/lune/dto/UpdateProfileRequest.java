package com.lune.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String nickname;
    private String avatar;
    private String gender;
    private String birthday;
    private String signature;
}
