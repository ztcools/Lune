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

@Component
public class ChatMemory {

    private static final Logger log = LoggerFactory.getLogger(ChatMemory.class);
    private static final String KEY_CHAT = "agent:chat:";
    private static final String KEY_CTX = "agent:context:";

    private final StringRedisTemplate redis;

    public ChatMemory(StringRedisTemplate redis) {
        this.redis = redis;
    }

    String key(Long userId) {
        return KEY_CHAT + userId + ":" + LocalDate.now();
    }

    public void save(Long userId, List<ChatMessage> messages) {
        try {
            if (messages.size() > 100) {
                messages = new ArrayList<>(messages.subList(messages.size() - 100, messages.size()));
            }
            var k = key(userId);
            redis.opsForValue().set(k, JSONUtil.toJsonStr(messages));
            long ttl = ChronoUnit.SECONDS.between(LocalDateTime.now(),
                    LocalDate.now().plusDays(1).atStartOfDay());
            if (ttl > 0) redis.expire(k, ttl, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Redis save failed: {}", e.getMessage());
        }
    }

    public List<ChatMessage> load(Long userId) {
        try {
            var json = redis.opsForValue().get(key(userId));
            if (json != null) return JSONUtil.toList(json, ChatMessage.class);
        } catch (Exception e) {
            log.warn("Redis load failed: {}", e.getMessage());
        }
        return new ArrayList<>();
    }

    public void clear(Long userId) {
        try { redis.delete(key(userId)); } catch (Exception e) { /* ignore */ }
    }

    public void setContextEnabled(Long userId, boolean enabled) {
        try { redis.opsForValue().set(KEY_CTX + userId, String.valueOf(enabled)); } catch (Exception e) { /* ignore */ }
    }

    public boolean isContextEnabled(Long userId) {
        try { return "true".equals(redis.opsForValue().get(KEY_CTX + userId)); } catch (Exception e) { return false; }
    }
}
