package com.qoocca.be.user.model;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisDao {

    private final RedisTemplate<String,String> redisTemplate;
    private final ValueOperations<String,String> values;

    public RedisDao(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = (RedisTemplate<String, String>) (Object) redisTemplate;
        this.values = this.redisTemplate.opsForValue();
    }

    public void setValues(String key, String data) {
        values.set(key, data);
    }

    public void setValues(String key, String data, Duration duration) {
        values.set(key, data, duration);
    }

    public Object getValues(String key) {
        return values.get(key);
    }

    // 데이터 삭제 (Refresh Token 삭제 시)
    public void deleteValues(String key) {
        redisTemplate.delete(key);
    }
}
