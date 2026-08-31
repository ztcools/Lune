package com.lune.agent.memory;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link UserPreference} 偏好持久化测试（Mock Redis）。
 */
class UserPreferenceTest {

    @Test
    void getDefaultsContainsSeedKeys() {
        Map<String, String> defaults = UserPreference.getDefaults();
        assertThat(defaults).containsKeys(
                "article_auto_title", "article_style", "essay_style",
                "record_style", "work_template", "project_template");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void getAllFallsBackToDefaultsWhenRedisEmpty() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        HashOperations hashOps = mock(HashOperations.class);
        when(redis.opsForHash()).thenReturn(hashOps);
        when(hashOps.entries(anyString())).thenReturn(Collections.emptyMap());

        UserPreference prefs = new UserPreference(redis);
        Map<String, String> result = prefs.getAll(1L);

        assertThat(result).isEqualTo(UserPreference.getDefaults());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void setDelegatesToRedisHash() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        HashOperations hashOps = mock(HashOperations.class);
        when(redis.opsForHash()).thenReturn(hashOps);

        UserPreference prefs = new UserPreference(redis);
        prefs.set(1L, "essay_style", "文艺风");

        verify(hashOps).put("agent:pref:1", "essay_style", "文艺风");
    }
}
