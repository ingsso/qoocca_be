package com.qoocca.teachers.api.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OpenAiProperties.class)
public class OpenAiConfig {
}
