package com.example.first.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class NaverNewsConfig {

    @Bean
    public RestTemplate newsTemplate() {
        return new RestTemplate();
    }

}
