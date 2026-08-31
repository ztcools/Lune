package com.lune.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SecurityUtils} 当前登录用户提取测试。
 */
class SecurityUtilsTest {

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsNullWhenNoAuthentication() {
        assertThat(SecurityUtils.getCurrentUserId()).isNull();
    }

    @Test
    void returnsUserIdFromClaimsPrincipal() {
        Claims claims = Jwts.claims()
                .subject("admin")
                .add("userId", 7L)
                .add("role", "ADMIN")
                .build();
        var auth = new UsernamePasswordAuthenticationToken(claims, null, java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(SecurityUtils.getCurrentUserId()).isEqualTo(7L);
    }

    @Test
    void returnsNullForNonClaimsPrincipal() {
        var auth = new UsernamePasswordAuthenticationToken("admin", null, java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(SecurityUtils.getCurrentUserId()).isNull();
    }
}
