package com.lune.agent.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户偏好持久化（Redis Hash，不过期）。
 *
 * <p>偏好不写在 system prompt 里——而是从 Redis 读取后作为工具调用的默认参数注入。
 * 用户可在前端偏好面板随时修改，Agent 下次调用立即生效。</p>
 *
 * <pre>
 * agent:pref:1 → {
 *   article_auto_title: "true",
 *   article_style: "简洁技术笔记",
 *   essay_default_location: "广州",
 *   essay_style: "朋友圈式，轻松口语",
 *   record_style: "短评，100字内",
 *   work_template: "STAR法则",
 *   project_template: "README式"
 * }
 * </pre>
 */
@Component
public class UserPreference {

    private static final Logger log = LoggerFactory.getLogger(UserPreference.class);
    private static final String KEY_PREFIX = "agent:pref:";

    private final StringRedisTemplate redis;

    public UserPreference(StringRedisTemplate redis) {
        this.redis = redis;
    }

    private String key(Long userId) {
        return KEY_PREFIX + userId;
    }

    /** 获取用户全部偏好 */
    public Map<String, String> getAll(Long userId) {
        try {
            var entries = redis.<String, String>opsForHash().entries(key(userId));
            if (entries != null && !entries.isEmpty()) {
                return new HashMap<>(entries);
            }
        } catch (Exception e) {
            log.warn("Failed to load preferences for user {}: {}", userId, e.getMessage());
        }
        return getDefaults();
    }

    /** 设置单条偏好 */
    public void set(Long userId, String field, String value) {
        try {
            redis.<String, String>opsForHash().put(key(userId), field, value);
        } catch (Exception e) {
            log.warn("Failed to save preference {} for user {}: {}", field, userId, e.getMessage());
        }
    }

    /** 批量设置偏好 */
    public void setAll(Long userId, Map<String, String> prefs) {
        try {
            redis.<String, String>opsForHash().putAll(key(userId), prefs);
        } catch (Exception e) {
            log.warn("Failed to save preferences for user {}: {}", userId, e.getMessage());
        }
    }

    /** 默认偏好（种子数据：风格偏好） */
    public static Map<String, String> getDefaults() {
        var defaults = new HashMap<String, String>();
        defaults.put("article_auto_title", "true");
        defaults.put("article_style", "简洁技术笔记");
        defaults.put("essay_default_location", "广州");
        defaults.put("essay_style", "朋友圈式，轻松口语");
        defaults.put("record_style", "短评，100字内");
        defaults.put("work_template", "STAR法则");
        defaults.put("project_template", "README式");
        return Collections.unmodifiableMap(defaults);
    }
}
