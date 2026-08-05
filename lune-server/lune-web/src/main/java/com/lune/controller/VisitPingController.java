package com.lune.controller;

import com.lune.common.ClientIp;
import com.lune.common.Result;
import com.lune.service.VisitLogRecorder;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 访问记录端点：按会话去重，30 分钟无活动算一次访问。
 *
 * <p>同 IP 的每次 ping 都会刷新 Redis TTL（延长会话窗口），
 * 只有 key 不存在时才记一条新 visit_log。
 * 这样：几分钟内跳转多页 → 一次；隔两小时回来 → 又一次。
 */
@RestController
@RequestMapping("/api/visit")
public class VisitPingController {

    private static final Logger log = LoggerFactory.getLogger(VisitPingController.class);
    private static final Duration SESSION_TTL = Duration.ofMinutes(30);

    private final VisitLogRecorder recorder;
    private final StringRedisTemplate redisTemplate;

    public VisitPingController(VisitLogRecorder recorder, StringRedisTemplate redisTemplate) {
        this.recorder = recorder;
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/ping")
    public Result<String> ping(HttpServletRequest request) {
        String ip = ClientIp.resolve(request);
        String ua = request.getHeader("User-Agent");
        String sessionKey = "visit:session:" + ip;

        try {
            // SET 而非 SETNX：每次 ping 都刷新会话 TTL，活跃浏览不中断
            Boolean existed = redisTemplate.hasKey(sessionKey);
            redisTemplate.opsForValue()
                .set(sessionKey, "1", SESSION_TTL.getSeconds(), TimeUnit.SECONDS);
            if (Boolean.FALSE.equals(existed)) {
                recorder.record(ip, ua, "/", "PING");
            }
        } catch (Exception e) {
            // Redis 异常时降级：直接记录
            log.debug("[VisitPing] Redis 异常，降级直接记录：{}", e.getMessage());
            recorder.record(ip, ua, "/", "PING");
        }

        return Result.success("ok");
    }
}
