package com.lune.service.impl;

import com.lune.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final RedisTemplate<String, String> redisTemplate;
    private final String from;

    public EmailServiceImpl(JavaMailSender mailSender,
                            RedisTemplate<String, String> redisTemplate,
                            @Value("${spring.mail.username}") String from) {
        this.mailSender = mailSender;
        this.redisTemplate = redisTemplate;
        this.from = from;
    }

    @Override
    public void sendVerificationCode(String toEmail) {
        String code = String.format("%06d", new SecureRandom().nextInt(1000000));
        redisTemplate.opsForValue().set("code:" + toEmail, code, 5, TimeUnit.MINUTES);
        var message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(toEmail);
        message.setSubject("Lune - 邮箱验证码");
        message.setText("你的验证码是：" + code + "，5分钟内有效。");
        mailSender.send(message);
    }

    @Override
    public boolean verifyCode(String email, String code) {
        String stored = redisTemplate.opsForValue().get("code:" + email);
        return stored != null && stored.equals(code);
    }

    @Override
    public void deleteCode(String email) {
        redisTemplate.delete("code:" + email);
    }
}
