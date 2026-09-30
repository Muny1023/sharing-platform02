package org.example.notificationservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class InternalSecurityConfiguration {
    @Bean
    InternalServiceTokenFilter internalServiceTokenFilter(
            @Value("${spring.cloud.internal.token:${INTERNAL_SERVICE_TOKEN:dev-internal-token}}") String token) {
        return new InternalServiceTokenFilter(token, List.of("/internal/notifications"));
    }
}
