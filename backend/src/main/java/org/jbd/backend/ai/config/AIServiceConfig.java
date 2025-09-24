package org.jbd.backend.ai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AIServiceConfig {

    @Value("${ai.service.base-url:http://localhost:8001}")
    private String aiServiceBaseUrl;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public String getAiServiceBaseUrl() {
        return aiServiceBaseUrl;
    }
}