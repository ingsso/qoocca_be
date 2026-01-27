package com.qoocca.teachers.auth.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.core.RedisTemplate;

@Component
public class RedisTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @PostConstruct
    public void testRedis() {
        try {
            // 테스트용 값 설정
            redisTemplate.opsForValue().set("ping", "pong");
            String value = (String) redisTemplate.opsForValue().get("ping");
            System.out.println("Redis 연결 성공: " + value);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Redis 연결 실패");
        }
    }
}
