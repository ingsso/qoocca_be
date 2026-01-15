package com.example.qoocca_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableJpaAuditing
public class QooccaBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(QooccaBeApplication.class, args);
    }

}
