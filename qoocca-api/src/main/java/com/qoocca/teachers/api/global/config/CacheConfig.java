package com.qoocca.teachers.api.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class CacheConfig {

    public static final String ME_ACADEMIES = "me:academies";
    public static final String DASHBOARD_STATS = "dashboard:stats";
    public static final String DASHBOARD_CLASS_SUMMARY = "dashboard:class-summary";
    public static final String ACADEMY_SUBJECTS = "academy:subjects";
    public static final String ACADEMY_AGES = "academy:ages";
    public static final String ATTENDANCE_SUMMARY = "attendance:summary";

    @Bean
    @Primary
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .activateDefaultTyping(
                        LaissezFaireSubTypeValidator.instance,
                        ObjectMapper.DefaultTyping.NON_FINAL,
                        JsonTypeInfo.As.PROPERTY
                );

        RedisCacheConfiguration baseConfig = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper)));

        Map<String, RedisCacheConfiguration> configs = new HashMap<>();
        configs.put(ME_ACADEMIES, baseConfig.entryTtl(Duration.ofMinutes(30)));
        configs.put(DASHBOARD_STATS, baseConfig.entryTtl(Duration.ofMinutes(2)));
        configs.put(DASHBOARD_CLASS_SUMMARY, baseConfig.entryTtl(Duration.ofMinutes(2)));
        configs.put(ACADEMY_SUBJECTS, baseConfig.entryTtl(Duration.ofHours(6)));
        configs.put(ACADEMY_AGES, baseConfig.entryTtl(Duration.ofHours(6)));
        configs.put(ATTENDANCE_SUMMARY, baseConfig.entryTtl(Duration.ofMinutes(2)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(baseConfig)
                .withInitialCacheConfigurations(configs)
                .build();
    }
}
