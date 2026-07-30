package com.lune.security;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类：从 SecurityContext 获取当前登录用户信息
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    /**
     * 获取当前登录用户的 ID
     */
    public static Long getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Claims claims) {
            return claims.get("userId", Long.class);
        }
        return null;
    }
}
