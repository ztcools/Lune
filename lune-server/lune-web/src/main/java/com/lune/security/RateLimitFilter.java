package com.lune.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lune.common.Result;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 接口限流过滤器（防爬虫 / 防刷 / 防爆破）。
 *
 * <p>优先使用 Redis 做分布式滑动窗口限流；Redis 不可用时降级为
 * 本地内存计数（单实例可用，重启清零）。规则：</p>
 * <ul>
 *   <li>敏感端点（登录/注册/发评论/发树洞/许愿）：严格限流</li>
 *   <li>其余 /api 读接口：宽松限流，防爬取</li>
 *   <li>静态资源、健康检查不限流</li>
 * </ul>
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    /** 写操作/敏感操作：每 IP 每分钟 */
    private static final int WRITE_LIMIT = 20;
    /** 登录注册：每 IP 每 5 分钟（防爆破） */
    private static final int AUTH_LIMIT = 10;
    /** 普通读接口：每 IP 每分钟 */
    private static final int READ_LIMIT = 120;

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Redis 不可用时的本地降级计数器: key -> [windowStart, count] */
    private final Map<String, long[]> localWindows = new ConcurrentHashMap<>();

    public RateLimitFilter(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // 静态资源与上传文件、健康检查不限流
        return !path.startsWith("/api/")
                || path.startsWith("/api/actuator/health");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        String ip = clientIp(request);

        int limit;
        long windowSeconds;
        String bucket;

        if (path.startsWith("/api/auth/")) {
            limit = AUTH_LIMIT;
            windowSeconds = 300;
            bucket = "auth";
        } else if ("POST".equals(method) || "PUT".equals(method)
                || "DELETE".equals(method) || "PATCH".equals(method)) {
            limit = WRITE_LIMIT;
            windowSeconds = 60;
            bucket = "write";
        } else {
            limit = READ_LIMIT;
            windowSeconds = 60;
            bucket = "read";
        }

        if (isAllowed(ip, bucket, limit, windowSeconds)) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("限流触发: ip={} path={} bucket={}", ip, path, bucket);
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setHeader("Retry-After", String.valueOf(windowSeconds));
            Result<Object> body = Result.fail(429, "请求过于频繁，请稍后再试");
            response.getWriter().write(objectMapper.writeValueAsString(body));
        }
    }

    /**
     * 滑动窗口计数（Redis 优先，本地降级）。
     */
    private boolean isAllowed(String ip, String bucket, int limit, long windowSeconds) {
        long now = System.currentTimeMillis();
        long windowStart = now - windowSeconds * 1000;
        String key = "ratelimit:" + bucket + ":" + ip;

        try {
            // Redis 滑动窗口（ZSET）
            var zset = redisTemplate.opsForZSet();
            zset.removeRangeByScore(key, 0, windowStart);
            Long count = zset.zCard(key);
            if (count != null && count >= limit) {
                return false;
            }
            zset.add(key, String.valueOf(now), now);
            redisTemplate.expire(key, Duration.ofSeconds(windowSeconds + 1));
            return true;
        } catch (Exception e) {
            // Redis 不可用，降级到本地固定窗口
            return localAllowed(ip + ":" + bucket, limit, windowSeconds, now);
        }
    }

    private boolean localAllowed(String key, int limit, long windowSeconds, long now) {
        long windowMs = windowSeconds * 1000;
        long[] entry = localWindows.compute(key, (k, v) -> {
            if (v == null || now - v[0] > windowMs) {
                return new long[]{now, 1};
            }
            v[1]++;
            return v;
        });
        // 简单清理，避免内存膨胀
        if (localWindows.size() > 10000) {
            localWindows.entrySet().removeIf(e -> now - e.getValue()[0] > windowMs);
        }
        return entry[1] <= limit;
    }

    private String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }
        return request.getRemoteAddr();
    }
}
