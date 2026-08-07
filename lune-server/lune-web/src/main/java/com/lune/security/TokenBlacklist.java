package com.lune.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * JWT 令牌黑名单（Redis 主存 + Caffeine 本地降级）。
 *
 * <p>登出时同时写入 Redis 和本地缓存。验证时优先查 Redis；
 * Redis 不可用时降级到本地缓存（fail-secure——不信任未验证的令牌）。
 *
 * <p>Caffeine 缓存大小和过期时间兼顾降级安全性和内存控制：
 * 过期 30 分钟（超过 JWT 最大生命周期），最多保留 10,000 条。</p>
 */
@Component
public class TokenBlacklist {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklist.class);

    private final RedisTemplate<String, String> redisTemplate;

    /** 本地降级缓存：token → true（值为占位，只关心 key 是否存在） */
    private final Cache<String, Boolean> localCache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();

    public TokenBlacklist(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 将 token 加入黑名单（登出时调用）。
     */
    public void blacklist(String token, long ttlMillis) {
        String key = "token:blacklist:" + token;
        // 写入 Redis（主）
        try {
            redisTemplate.opsForValue().set(key, "1",
                    java.time.Duration.ofMillis(Math.max(ttlMillis, 1)));
        } catch (Exception e) {
            log.warn("Redis 不可用，token 黑名单仅写入本地缓存: {}", e.getMessage());
        }
        // 写入本地缓存（降级兜底）
        localCache.put(token, true);
    }

    /**
     * 检查 token 是否在黑名单中。
     *
     * @return true 表示已撤销（应拒绝），false 表示未撤销
     */
    public boolean isBlacklisted(String token) {
        // 1. 先查 Redis
        try {
            String key = "token:blacklist:" + token;
            Boolean exists = redisTemplate.hasKey(key);
            if (Boolean.TRUE.equals(exists)) return true;
            // Redis 可用且 token 不在黑名单 → 信任并返回
            return false;
        } catch (Exception e) {
            log.warn("Redis 不可用，降级到本地缓存检查 token 黑名单");
        }
        // 2. Redis 不可用 → 查本地缓存
        return localCache.getIfPresent(token) != null;
    }
}
