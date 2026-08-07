package com.lune.agent.memory;

import cn.hutool.json.JSONUtil;
import com.lune.agent.dto.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 对话记忆系统（Redis 持久化）。
 *
 * <p>支持多会话隔离：{@code agent:chat:{userId}:{sessionId}:{date}}，
 * 默认 session 为 "default"。按天 TTL 自动过期。</p>
 */
@Component
public class ChatMemory {

    private static final Logger log = LoggerFactory.getLogger(ChatMemory.class);
    private static final String KEY_CHAT = "agent:chat:";
    private static final String KEY_CTX = "agent:context:";
    private static final String DEFAULT_SESSION = "default";

    private final StringRedisTemplate redis;

    public ChatMemory(StringRedisTemplate redis) {
        this.redis = redis;
    }

    // ── Session-aware keys ──

    String key(Long userId, String sessionId) {
        var sid = sessionId != null && !sessionId.isBlank() ? sessionId : DEFAULT_SESSION;
        return KEY_CHAT + userId + ":" + sid + ":" + LocalDate.now();
    }

    String key(Long userId) {
        return key(userId, DEFAULT_SESSION);
    }

    // ── Core operations ──

    public void save(Long userId, List<ChatMessage> messages) {
        save(userId, DEFAULT_SESSION, messages);
    }

    public void save(Long userId, String sessionId, List<ChatMessage> messages) {
        try {
            if (messages.size() > 100) {
                messages = new ArrayList<>(messages.subList(messages.size() - 100, messages.size()));
            }
            var k = key(userId, sessionId);
            redis.opsForValue().set(k, JSONUtil.toJsonStr(messages));
            long ttl = ChronoUnit.SECONDS.between(LocalDateTime.now(),
                    LocalDate.now().plusDays(1).atStartOfDay());
            if (ttl > 0) redis.expire(k, ttl, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Redis save failed for user={} session={}: {}", userId, sessionId, e.getMessage());
        }
    }

    public List<ChatMessage> load(Long userId) {
        return load(userId, DEFAULT_SESSION);
    }

    public List<ChatMessage> load(Long userId, String sessionId) {
        try {
            var json = redis.opsForValue().get(key(userId, sessionId));
            if (json != null) return JSONUtil.toList(json, ChatMessage.class);
        } catch (Exception e) {
            log.warn("Redis load failed for user={} session={}: {}", userId, sessionId, e.getMessage());
        }
        return new ArrayList<>();
    }

    public void clear(Long userId) {
        clear(userId, DEFAULT_SESSION);
    }

    public void clear(Long userId, String sessionId) {
        try {
            redis.delete(key(userId, sessionId));
        } catch (Exception e) {
            log.warn("Redis clear failed for user={} session={}: {}", userId, sessionId, e.getMessage());
        }
    }

    // ── Context toggle ──

    public void setContextEnabled(Long userId, boolean enabled) {
        try {
            redis.opsForValue().set(KEY_CTX + userId, String.valueOf(enabled));
        } catch (Exception e) { log.warn("Redis context toggle failed: {}", e.getMessage()); }
    }

    public boolean isContextEnabled(Long userId) {
        try {
            return "true".equals(redis.opsForValue().get(KEY_CTX + userId));
        } catch (Exception e) {
            log.warn("Redis context check failed: {}", e.getMessage());
            return true; // Redis 不可用时默认开启上下文，不阻碍用户体验
        }
    }

    // ── Session listing ──

    /** 列出用户所有活跃 session（当天有对话的） */
    public List<String> listSessions(Long userId) {
        try {
            var pattern = KEY_CHAT + userId + ":*:" + LocalDate.now();
            var keys = redis.keys(pattern);
            if (keys == null || keys.isEmpty()) return List.of();
            return keys.stream()
                    .map(k -> {
                        // Format: agent:chat:{userId}:{sessionId}:{date}
                        var parts = k.split(":");
                        if (parts.length >= 4) return parts[parts.length - 2];
                        return k;
                    })
                    .distinct()
                    .toList();
        } catch (Exception e) {
            log.warn("Redis session list failed: {}", e.getMessage());
            return List.of();
        }
    }
}
