package com.lune.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link JwtTokenProvider} 签发/解析/校验测试。
 */
class JwtTokenProviderTest {

    private static final String SECRET = "unit-test-secret-0123456789abcdef0123456789";

    private JwtTokenProvider provider() {
        return new JwtTokenProvider(SECRET, 60_000L);
    }

    @Test
    void createAndParseTokenRoundTrip() {
        JwtTokenProvider provider = provider();
        String token = provider.createToken(42L, "admin", "ADMIN");

        Claims claims = provider.parseToken(token);
        assertThat(claims.getSubject()).isEqualTo("admin");
        assertThat(claims.get("userId", Long.class)).isEqualTo(42L);
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
    }

    @Test
    void validateTokenAcceptsValidToken() {
        JwtTokenProvider provider = provider();
        String token = provider.createToken(1L, "user", "USER");
        assertThat(provider.validateToken(token)).isTrue();
    }

    @Test
    void validateTokenRejectsGarbage() {
        assertThat(provider().validateToken("not-a-jwt")).isFalse();
    }

    @Test
    void validateTokenRejectsTokenSignedWithDifferentKey() {
        JwtTokenProvider other = new JwtTokenProvider("another-secret-0123456789abcdef0123456789", 60_000L);
        String token = other.createToken(1L, "user", "USER");
        assertThat(provider().validateToken(token)).isFalse();
    }

    @Test
    void constructorRejectsShortSecret() {
        assertThatThrownBy(() -> new JwtTokenProvider("too-short", 60_000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("JWT_SECRET");
    }
}
