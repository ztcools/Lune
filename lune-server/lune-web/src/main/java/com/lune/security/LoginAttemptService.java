package com.lune.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 登录失败锁定（防暴力破解）。
 *
 * <p>同一账号连续失败 {@value #MAX_ATTEMPTS} 次后锁定
 * {@value #LOCK_MINUTES} 分钟。优先 Redis 存储，Redis 不可用降级内存。</p>
 */
@Service
public class LoginAttemptService {

    private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_MINUTES = 15;

    private static final String ATTEMPT_KEY = "login:attempts:";
    private static final String LOCK_KEY = "login:lock:";

    private final RedisTemplate<String, String> redisTemplate;

    /** 本地降级：account -> 失败次数 */
    private final Map<String, AtomicInteger> localAttempts = new ConcurrentHashMap<>();
    /** 本地降级：account -> 锁定截止时间戳 */
    private final Map<String, Long> localLocks = new ConcurrentHashMap<>();

    public LoginAttemptService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /** 账号当前是否被锁定 */
    public boolean isLocked(String account) {
        try {
            String locked = redisTemplate.opsForValue().get(LOCK_KEY + account);
            return locked != null;
        } catch (Exception e) {
            Long until = localLocks.get(account);
            if (until != null && until > System.currentTimeMillis()) {
                return true;
            }
            localLocks.remove(account);
            return false;
        }
    }

    /** 记录一次登录失败，返回剩余可尝试次数（0 表示已锁定） */
    public int onLoginFailed(String account) {
        try {
            Long count = redisTemplate.opsForValue().increment(ATTEMPT_KEY + account);
            if (count != null && count == 1L) {
                redisTemplate.expire(ATTEMPT_KEY + account, Duration.ofMinutes(LOCK_MINUTES));
            }
            if (count != null && count >= MAX_ATTEMPTS) {
                redisTemplate.opsForValue().set(LOCK_KEY + account, "1", Duration.ofMinutes(LOCK_MINUTES));
                redisTemplate.delete(ATTEMPT_KEY + account);
                log.warn("账号因连续失败被锁定: {}", account);
                return 0;
            }
            return (int) (MAX_ATTEMPTS - (count == null ? 0 : count));
        } catch (Exception e) {
            int count = localAttempts.computeIfAbsent(account, k -> new AtomicInteger(0)).incrementAndGet();
            if (count >= MAX_ATTEMPTS) {
                localLocks.put(account, System.currentTimeMillis() + LOCK_MINUTES * 60_000);
                localAttempts.remove(account);
                log.warn("账号因连续失败被锁定(本地): {}", account);
                return 0;
            }
            return MAX_ATTEMPTS - count;
        }
    }

    /** 登录成功，清除失败记录 */
    public void onLoginSuccess(String account) {
        try {
            redisTemplate.delete(ATTEMPT_KEY + account);
            redisTemplate.delete(LOCK_KEY + account);
        } catch (Exception e) {
            localAttempts.remove(account);
            localLocks.remove(account);
        }
    }
}
