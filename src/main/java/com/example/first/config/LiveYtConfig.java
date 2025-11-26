package com.example.first.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class LiveYtConfig {
    @Bean
    public RestTemplate ytTemplate(RestTemplateBuilder builder) {
        return builder
                // 연결이 5초 이상 걸리면 강제로 끊어버립니다. (서버 다운 방지)
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }
}
