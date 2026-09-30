package org.example.notificationservice.config;

import feign.RequestTemplate;
import org.example.cloudcommon.InternalServiceHeaders;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InternalFeignRequestInterceptorTest {
    @Test
    void addsServiceTokenForBackgroundCalls() {
        RequestTemplate template = new RequestTemplate();

        new InternalFeignRequestInterceptor("secret").apply(template);

        assertEquals("secret", template.headers().get(InternalServiceHeaders.INTERNAL_TOKEN)
                .iterator().next());
    }
}
