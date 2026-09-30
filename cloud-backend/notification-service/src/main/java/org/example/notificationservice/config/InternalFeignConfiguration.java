package org.example.notificationservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InternalFeignConfiguration {
    @Bean
    InternalFeignRequestInterceptor internalFeignRequestInterceptor(
            @Value("${spring.cloud.internal.token:${INTERNAL_SERVICE_TOKEN:dev-internal-token}}") String token) {
        return new InternalFeignRequestInterceptor(token);
    }
}
