package com.lune.service;

public interface EmailService {
    void sendVerificationCode(String toEmail);
    boolean verifyCode(String email, String code);
    void deleteCode(String email);
}
