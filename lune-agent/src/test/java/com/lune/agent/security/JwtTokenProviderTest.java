package com.lune.agent.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link JwtTokenProvider} 校验 lune-server 签发 token 的测试。
 */
class JwtTokenProviderTest {

    private static final String SECRET = "unit-test-secret-0123456789abcdef0123456789";

    private JwtTokenProvider provider(String secret) {
        return new JwtTokenProvider(secret);
    }

    private String signWith(String secret) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        return Jwts.builder()
                .subject("admin")
                .claim("userId", 1L)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + 60_000))
                .signWith(key)
                .compact();
    }

    @Test
    void validatesTokenSignedWithSameSecret() {
        JwtTokenProvider provider = provider(SECRET);
        assertThat(provider.validateToken(signWith(SECRET))).isTrue();
    }

    @Test
    void rejectsTokenSignedWithDifferentSecret() {
        JwtTokenProvider provider = provider(SECRET);
        assertThat(provider.validateToken(signWith("another-secret-0123456789abcdef0123456789"))).isFalse();
    }

    @Test
    void rejectsGarbage() {
        assertThat(provider(SECRET).validateToken("not-a-jwt")).isFalse();
    }

    @Test
    void rejectsShortSecret() {
        assertThatThrownBy(() -> provider("short"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("JWT_SECRET");
    }
}
