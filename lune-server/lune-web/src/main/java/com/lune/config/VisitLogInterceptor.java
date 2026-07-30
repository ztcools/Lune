package com.lune.config;

import com.lune.common.ClientIp;
import com.lune.service.VisitLogRecorder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 访问日志拦截器
 * 自动记录每个公共 API 的访问，含 IP 地理位置
 *
 * 过滤规则：
 * - 排除 /api/admin/**（后台）
 * - 排除 /api/auth/**（认证）
 * - 排除 /api/actuator/**（健康检查）
 * - 排除 /upload/**（静态资源）
 * - 排除静态文件扩展（.js/.css/.png/.jpg/.svg/.woff/.woff2/.ico）
 *
 * 防刷：同 IP + 同 path 10 秒内只记 1 次（Redis SETNX）
 */
@Component
public class VisitLogInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(VisitLogInterceptor.class);
    private static final Set<String> EXCLUDE_PREFIX = Set.of(
        "/api/admin", "/api/auth", "/api/actuator", "/upload", "/error"
    );
    private static final Set<String> STATIC_EXT = Set.of(
        ".js", ".css", ".png", ".jpg", ".jpeg", ".gif", ".svg", ".ico",
        ".woff", ".woff2", ".ttf", ".eot", ".map", ".webp", ".mp3", ".mp4"
    );
    private static final Duration ANTI_SPAM_WINDOW = Duration.ofSeconds(10);

    private final VisitLogRecorder recorder;
    private final StringRedisTemplate redisTemplate;

    public VisitLogInterceptor(VisitLogRecorder recorder, StringRedisTemplate redisTemplate) {
        this.recorder = recorder;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();

        // 过滤排除路径
        if (shouldExclude(path)) return true;

        // 获取真实 IP（考虑反向代理）
        String ip = ClientIp.resolve(request);
        String ua = request.getHeader("User-Agent");
        String method = request.getMethod();

        // 防刷：同 IP 同 path 10 秒内只记 1 次
        String dedupKey = "visit:dedup:" + ip + ":" + path;
        try {
            Boolean isFirst = redisTemplate.opsForValue()
                .setIfAbsent(dedupKey, "1", ANTI_SPAM_WINDOW.getSeconds(), TimeUnit.SECONDS);
            if (!Boolean.TRUE.equals(isFirst)) {
                return true; // 10 秒内重复访问，不记录
            }
        } catch (Exception e) {
            // Redis 异常不影响访问，继续记录
            log.debug("[VisitLog] Redis 异常：{}", e.getMessage());
        }

        // 跨 Bean 调用，@Async 才会生效（真正异步，不阻塞请求线程）
        recorder.record(ip, ua, path, method);
        return true;
    }

    private boolean shouldExclude(String path) {
        if (path == null) return true;
        for (String prefix : EXCLUDE_PREFIX) {
            if (path.startsWith(prefix)) return true;
        }
        // 排除静态资源
        int dotIdx = path.lastIndexOf('.');
        if (dotIdx > 0) {
            String ext = path.substring(dotIdx).toLowerCase();
            if (STATIC_EXT.contains(ext)) return true;
        }
        return false;
    }

}
