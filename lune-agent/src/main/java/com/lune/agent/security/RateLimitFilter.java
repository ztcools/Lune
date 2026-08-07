package com.lune.agent.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agent 服务限流过滤器。
 *
 * <p>重点保护 LLM 调用端点（/api/admin/agent/chat），防止 API 费用滥用。
 * 其他 admin 端点宽松限流。Redis 滑动窗口优先，本地固定窗口降级。</p>
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    /** Agent 聊天：每 IP 每分钟 10 次（LLM API 有费用） */
    private static final int CHAT_LIMIT = 10;
    /** 其他 admin 操作：每 IP 每分钟 30 次 */
    private static final int ADMIN_LIMIT = 30;

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 本地降级计数器 */
    private final Map<String, long[]> localWindows = new ConcurrentHashMap<>();

    public RateLimitFilter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/actuator/health");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String ip = resolveIp(request);

        int limit;
        String bucket;
        if (path.startsWith("/api/admin/agent/chat")) {
            limit = CHAT_LIMIT;
            bucket = "chat";
        } else {
            limit = ADMIN_LIMIT;
            bucket = "admin";
        }

        if (isAllowed(ip, bucket, limit, 60)) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("Agent rate limit: ip={} path={} bucket={}", ip, path, bucket);
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setHeader("Retry-After", "60");
            var body = Map.of("code", 429, "message", "请求过于频繁，请稍后再试");
            response.getWriter().write(objectMapper.writeValueAsString(body));
        }
    }

    private boolean isAllowed(String ip, String bucket, int limit, long windowSeconds) {
        long now = System.currentTimeMillis();
        long windowStart = now - windowSeconds * 1000;
        String key = "ratelimit:agent:" + bucket + ":" + ip;

        try {
            var zset = redis.opsForZSet();
            zset.removeRangeByScore(key, 0, windowStart);
            Long count = zset.zCard(key);
            if (count != null && count >= limit) return false;
            zset.add(key, String.valueOf(now), now);
            redis.expire(key, Duration.ofSeconds(windowSeconds + 1));
            return true;
        } catch (Exception e) {
            return localAllowed(ip + ":" + bucket, limit, windowSeconds, now);
        }
    }

    private boolean localAllowed(String key, int limit, long windowSeconds, long now) {
        long windowMs = windowSeconds * 1000;
        long[] entry = localWindows.compute(key, (k, v) -> {
            if (v == null || now - v[0] > windowMs) return new long[]{now, 1};
            v[1]++;
            return v;
        });
        if (localWindows.size() > 5000) {
            localWindows.entrySet().removeIf(e -> now - e.getValue()[0] > windowMs);
        }
        return entry[1] <= limit;
    }

    private String resolveIp(HttpServletRequest request) {
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank() && !"unknown".equalsIgnoreCase(realIp.trim())) {
            return realIp.trim();
        }
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            int idx = xff.lastIndexOf(',');
            return (idx >= 0 ? xff.substring(idx + 1) : xff).trim();
        }
        return request.getRemoteAddr();
    }
}
