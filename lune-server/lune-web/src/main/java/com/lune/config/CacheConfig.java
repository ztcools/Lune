package com.lune.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * 多层缓存配置（Redis JSON 序列化 + Caffeine 本地降级）。
 *
 * <p>使用 GenericJackson2JsonRedisSerializer（注册了 JavaTimeModule）
 * 序列化缓存值，实体类无需实现 {@link java.io.Serializable}，
 * LocalDateTime 等 Java 8 时间类型也能正常序列化。
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // ObjectMapper 注册 JavaTimeModule（支持 LocalDateTime 序列化）
        var mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);

        var jsonSerializer = new GenericJackson2JsonRedisSerializer(mapper);
        var stringSerializer = new StringRedisSerializer();

        var keySerializationPair = RedisSerializationContext.SerializationPair.fromSerializer(stringSerializer);
        var valueSerializationPair = RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .prefixCacheNameWith("lune:cache:")
                .serializeKeysWith(keySerializationPair)
                .serializeValuesWith(valueSerializationPair);

        RedisCacheConfiguration articlesConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .prefixCacheNameWith("lune:cache:")
                .serializeKeysWith(keySerializationPair)
                .serializeValuesWith(valueSerializationPair);

        RedisCacheManager redisCacheManager = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration("articles", articlesConfig)
                .build();

        // Caffeine 本地缓存（Redis 不可用时的降级兜底）
        CaffeineCacheManager localCacheManager = new CaffeineCacheManager();
        localCacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(10)));

        // 组合：优先 Redis，Redis 不可用时降级到 Caffeine
        var composite = new org.springframework.cache.support.CompositeCacheManager(
                redisCacheManager, localCacheManager);
        composite.setFallbackToNoOpCache(false);
        return composite;
    }
}
