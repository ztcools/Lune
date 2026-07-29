package com.lune.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 统一安全响应头。
 *
 * <p>Nginx 层也设置了基础安全头，这里在后端再兜底一份，
 * 保证即使绕过 Nginx 直连后端（内网/测试）也有防护。</p>
 */
@Component
public class SecurityHeadersFilter extends OncePerRequestFilter {

    /** 是否开启 HSTS（仅 HTTPS 生产环境开启，由配置注入） */
    @Value("${app.security.hsts-enabled:false}")
    private boolean hstsEnabled;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "SAMEORIGIN");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        response.setHeader("Permissions-Policy",
                "geolocation=(), microphone=(), camera=(), payment=()");
        // CSP：允许同源 + 图片/媒体 https + 必要的内联样式（Element Plus 运行时）
        response.setHeader("Content-Security-Policy",
                "default-src 'self'; "
                + "img-src 'self' data: https:; "
                + "media-src 'self' https:; "
                + "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; "
                + "font-src 'self' https://fonts.gstatic.com; "
                + "script-src 'self'; "
                + "connect-src 'self'; "
                + "object-src 'none'; "
                + "frame-ancestors 'self'; "
                + "base-uri 'self'");
        if (hstsEnabled) {
            response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        }
        filterChain.doFilter(request, response);
    }
}
