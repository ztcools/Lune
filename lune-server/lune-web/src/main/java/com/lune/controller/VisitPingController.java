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
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

/**
 * 访问记录端点：前端每天 ping 一次，后端记一条 visit_log。
 *
 * <p>之前是拦截器按请求路径（10s 窗口去重）记录，用户反复跳转页面会产生大量重复行。
 * 这里改为行业更常见的「每日访客计数」模式：同 IP 每天只记一条，path 固定为 "/"。
 */
@RestController
@RequestMapping("/api/visit")
public class VisitPingController {

    private static final Logger log = LoggerFactory.getLogger(VisitPingController.class);
    private static final Duration PING_WINDOW = Duration.ofDays(1);

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
        String today = LocalDate.now().toString();
        String dedupKey = "visit:ping:" + ip + ":" + today;

        try {
            Boolean isFirst = redisTemplate.opsForValue()
                .setIfAbsent(dedupKey, "1", PING_WINDOW.getSeconds(), TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(isFirst)) {
                recorder.record(ip, ua, "/", "PING");
            }
        } catch (Exception e) {
            // Redis 异常时降级：直接记录（同 IP 一天可能写多条，但比漏记强）
            log.debug("[VisitPing] Redis 异常，降级直接记录：{}", e.getMessage());
            recorder.record(ip, ua, "/", "PING");
        }

        return Result.success("ok");
    }
}
