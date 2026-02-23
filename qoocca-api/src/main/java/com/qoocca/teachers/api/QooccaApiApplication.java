package com.qoocca.teachers.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;


import org.springframework.retry.annotation.EnableRetry;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAsync
@SpringBootApplication(scanBasePackages = "com.qoocca.teachers") // 전체 패키지 스캔
@EnableJpaRepositories(basePackages = "com.qoocca.teachers")    // 리포지토리 스캔
@EntityScan(basePackages = "com.qoocca.teachers")
@EnableTransactionManagement
@EnableCaching
@EnableRetry
public class QooccaApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(QooccaApiApplication.class, args);
    }

}
