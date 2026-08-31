package com.lune.agent.memory;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link ChatMemory} 对话记忆测试（Mock Redis）。
 */
class ChatMemoryTest {

    @Test
    void keyContainsUserIdAndSession() {
        ChatMemory memory = new ChatMemory(mock(StringRedisTemplate.class));
        String key = memory.key(42L, "abc");
        assertThat(key).startsWith("agent:chat:42:abc:");
    }

    @Test
    void defaultSessionWhenBlank() {
        ChatMemory memory = new ChatMemory(mock(StringRedisTemplate.class));
        assertThat(memory.key(7L, "  ")).startsWith("agent:chat:7:default:");
    }

    @Test
    @SuppressWarnings("unchecked")
    void loadReturnsEmptyListWhenRedisHasNoValue() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(anyString())).thenReturn(null);

        ChatMemory memory = new ChatMemory(redis);
        List<?> result = memory.load(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void isContextEnabledReturnsFalseWhenNotSet() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(anyString())).thenReturn(null);

        assertThat(new ChatMemory(redis).isContextEnabled(1L)).isFalse();
    }

    @Test
    @SuppressWarnings("unchecked")
    void isContextEnabledDefaultsToTrueWhenRedisDown() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        when(redis.opsForValue()).thenThrow(new RuntimeException("redis down"));

        assertThat(new ChatMemory(redis).isContextEnabled(1L)).isTrue();
    }
}
