package com.qoocca.teachers.api.global.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(OpenAiProperties.class)
public class OpenAiConfig {

    @Bean("openAiRestTemplate")
    public RestTemplate openAiRestTemplate(RestTemplateBuilder builder, OpenAiProperties properties) {
        return builder
                .setConnectTimeout(Duration.ofMillis(properties.getConnectTimeoutMillis()))
                .setReadTimeout(Duration.ofMillis(properties.getReadTimeoutMillis()))
                .build();
    }
}
