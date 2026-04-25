package com.example.LoanManagementApp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import java.time.Duration;

/**
 * REST Client Configuration
 * Configures RestTemplate for external API calls (Payment Gateways, etc.)
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                // Fail fast so API Gateway can return a useful 503 instead of timing out with 504.
                .setConnectTimeout(Duration.ofSeconds(4))
                .setReadTimeout(Duration.ofSeconds(8))
                .build();
    }
}
