package com.lune.agent.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * JWT token provider — validates tokens issued by lune-server.
 * Shares the same secret ({@code app.jwt.secret}) so tokens from the
 * main application are accepted here without re-issuing.
 */
@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);
    private static final String DEV_DEFAULT = "dev-secret-only-for-local-development";

    private final SecretKey key;

    public JwtTokenProvider(@Value("${app.jwt.secret}") String secret) {
        // 与 lune-server 一致：密钥不足 32 字符直接启动失败，避免静默签名不匹配
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException(
                    "JWT_SECRET 必须至少 32 个字符，当前长度: " + (secret == null ? 0 : secret.length()));
        }
        if (DEV_DEFAULT.equals(secret)) {
            log.warn("正在使用默认开发密钥 JWT_SECRET —— 与后端签名不匹配会导致所有 /api/admin/agent/** 返回 403，"
                    + "请通过 --env-file .env.local 启动或显式设置 JWT_SECRET");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
