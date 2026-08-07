package com.lune.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * CORS 跨域配置（环境变量驱动）。
 *
 * <p>生产环境通过 {@code CORS_ALLOWED_ORIGINS} 指定精确域名；
 * 开发环境默认允许 localhost 常用端口。
 * 不再使用 {@code addAllowedOriginPattern("*")} 通配符，
 * 避免任意源都能调用 API 的安全风险。</p>
 */
@Configuration
public class CorsConfig {

    private static final Logger log = LoggerFactory.getLogger(CorsConfig.class);

    /** 允许的 HTTP 方法白名单 */
    private static final List<String> ALLOWED_METHODS = List.of(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");

    /** 允许的请求头白名单 */
    private static final List<String> ALLOWED_HEADERS = List.of(
            "Authorization", "Content-Type", "X-User-Id", "X-Requested-With");

    @Bean
    public CorsFilter corsFilter(
            @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:8080}") String origins) {
        var config = new CorsConfiguration();
        for (String origin : origins.split(",")) {
            String trimmed = origin.trim();
            if (!trimmed.isEmpty()) {
                config.addAllowedOrigin(trimmed);
            }
        }
        config.setAllowedMethods(ALLOWED_METHODS);
        config.setAllowedHeaders(ALLOWED_HEADERS);
        config.setAllowCredentials(true);
        config.setMaxAge(3600L); // 预检缓存 1 小时

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        log.info("CORS allowed origins: {}", origins);
        return new CorsFilter(source);
    }
}
