package com.qoocca.teachers.api.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);      // 기본 일꾼 20명
        executor.setMaxPoolSize(100);     // 최대 100명까지 증원
        executor.setQueueCapacity(200);   // 대기 줄 200명
        executor.setThreadNamePrefix("QooccaAsync-");
        executor.initialize();
        return executor;
    }
}