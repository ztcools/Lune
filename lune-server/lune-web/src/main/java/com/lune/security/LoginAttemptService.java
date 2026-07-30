package com.lune.security;

import com.lune.common.ClientIp;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 登录失败锁定（防暴力破解）。
 *
 * <p>连续失败 {@value #MAX_ATTEMPTS} 次后锁定 {@value #LOCK_MINUTES} 分钟。
 * 优先 Redis 存储，Redis 不可用时降级到内存。</p>
 *
 * <p><b>锁定维度是「账号 + 来源 IP」，不是账号本身。</b>
 * 仅按账号锁定会变成一个人人可用的拒绝服务开关：攻击者对 {@code admin}
 * 连发 5 次错误密码即可把站长锁在门外，循环重放就是永久锁定——
 * 防爆破措施本身成了更好用的攻击手段。加入 IP 维度后，攻击者只能锁死自己那条来源，
 * 站长从其它 IP 不受影响；针对单账号的分布式爆破则由
 * nginx（auth 5r/m）与 {@link RateLimitFilter} 的按 IP 限流兜住。</p>
 *
 * <p>账号一律小写归一：MySQL 默认排序规则不区分大小写，
 * {@code admin} 与 {@code Admin} 命中同一用户，若不归一化则每种大小写写法
 * 都能各拿一份 5 次预算，等于把爆破上限翻了若干倍。</p>
 *
 * <p>失败计数对「不存在的账号」同样累加：若只对存在的账号计数，
 * 返回的剩余次数就会成为账号是否存在的判别信号（存在的会递减、不存在的恒定），
 * 白送一个账号枚举接口。</p>
 */
@Service
public class LoginAttemptService {

    private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_MINUTES = 15;

    private static final String ATTEMPT_KEY = "login:attempts:";
    private static final String LOCK_KEY = "login:lock:";

    /** 降级容器容量上限：Redis 挂掉时不能让内存被无限增长的失败记录吃掉 */
    private static final int LOCAL_MAX_ENTRIES = 5000;

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectProvider<HttpServletRequest> requestProvider;

    /** 本地降级：identity -> 失败次数 */
    private final Map<String, AtomicInteger> localAttempts = new ConcurrentHashMap<>();
    /** 本地降级：identity -> 锁定截止时间戳 */
    private final Map<String, Long> localLocks = new ConcurrentHashMap<>();

    public LoginAttemptService(RedisTemplate<String, String> redisTemplate,
                              ObjectProvider<HttpServletRequest> requestProvider) {
        this.redisTemplate = redisTemplate;
        this.requestProvider = requestProvider;
    }

    /**
     * 锁定身份 = 小写账号 + 来源 IP。
     * 取不到请求上下文时退化为仅账号维度。
     */
    private String identity(String account) {
        String acct = account == null ? "" : account.trim().toLowerCase();
        HttpServletRequest req = requestProvider.getIfAvailable();
        String ip = req == null ? "-" : ClientIp.resolve(req);
        return acct + "|" + ip;
    }

    /** 当前账号+IP 是否被锁定 */
    public boolean isLocked(String account) {
        String id = identity(account);
        try {
            return redisTemplate.opsForValue().get(LOCK_KEY + id) != null;
        } catch (Exception e) {
            Long until = localLocks.get(id);
            if (until != null && until > System.currentTimeMillis()) {
                return true;
            }
            localLocks.remove(id);
            return false;
        }
    }

    /** 记录一次登录失败，返回剩余可尝试次数（0 表示已锁定） */
    public int onLoginFailed(String account) {
        String id = identity(account);
        try {
            Long count = redisTemplate.opsForValue().increment(ATTEMPT_KEY + id);
            if (count != null && count == 1L) {
                redisTemplate.expire(ATTEMPT_KEY + id, Duration.ofMinutes(LOCK_MINUTES));
            }
            if (count != null && count >= MAX_ATTEMPTS) {
                redisTemplate.opsForValue().set(LOCK_KEY + id, "1", Duration.ofMinutes(LOCK_MINUTES));
                redisTemplate.delete(ATTEMPT_KEY + id);
                log.warn("登录失败过多，已临时锁定: {}", id);
                return 0;
            }
            return (int) (MAX_ATTEMPTS - (count == null ? 0 : count));
        } catch (Exception e) {
            evictLocalIfNeeded();
            int count = localAttempts.computeIfAbsent(id, k -> new AtomicInteger(0)).incrementAndGet();
            if (count >= MAX_ATTEMPTS) {
                localLocks.put(id, System.currentTimeMillis() + LOCK_MINUTES * 60_000);
                localAttempts.remove(id);
                log.warn("登录失败过多，已临时锁定(本地): {}", id);
                return 0;
            }
            return MAX_ATTEMPTS - count;
        }
    }

    /** 登录成功，清除失败记录 */
    public void onLoginSuccess(String account) {
        String id = identity(account);
        try {
            redisTemplate.delete(ATTEMPT_KEY + id);
            redisTemplate.delete(LOCK_KEY + id);
        } catch (Exception e) {
            localAttempts.remove(id);
            localLocks.remove(id);
        }
    }

    /**
     * 降级路径的内存回收。Redis 不可用期间，攻击者用随机账号名即可让
     * 两个 Map 无限增长（堆只有 512M），因此先清过期锁，仍超限则整体丢弃计数——
     * 宁可丢失防爆破的计数精度，也不能把进程拖进 OOM。
     */
    private void evictLocalIfNeeded() {
        if (localAttempts.size() < LOCAL_MAX_ENTRIES && localLocks.size() < LOCAL_MAX_ENTRIES) {
            return;
        }
        long now = System.currentTimeMillis();
        localLocks.entrySet().removeIf(e -> e.getValue() <= now);
        if (localAttempts.size() >= LOCAL_MAX_ENTRIES) {
            localAttempts.clear();
            log.warn("本地登录失败计数超过 {} 条，已整体清空以保护内存", LOCAL_MAX_ENTRIES);
        }
    }
}
