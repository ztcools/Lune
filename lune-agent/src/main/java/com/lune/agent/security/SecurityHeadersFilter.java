package com.lune.agent.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 统一安全响应头（与 lune-web 保持一致）。
 */
@Component
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "SAMEORIGIN");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        response.setHeader("Permissions-Policy",
                "geolocation=(), microphone=(), camera=(), payment=()");
        response.setHeader("Content-Security-Policy",
                "default-src 'self'; "
                + "img-src 'self' data: https:; "
                + "media-src 'self' https:; "
                + "style-src 'self' 'unsafe-inline'; "
                + "font-src 'self'; "
                + "script-src 'self'; "
                + "connect-src 'self'; "
                + "object-src 'none'; "
                + "frame-ancestors 'self'; "
                + "base-uri 'self'");
        filterChain.doFilter(request, response);
    }
}
