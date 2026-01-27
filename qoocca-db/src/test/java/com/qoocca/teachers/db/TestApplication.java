package com.qoocca.teachers.db;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootConfiguration
@EnableAutoConfiguration
@EntityScan("com.qoocca.teachers.db")
@EnableJpaRepositories("com.qoocca.teachers.db")
public class TestApplication {
}
