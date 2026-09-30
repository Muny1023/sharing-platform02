package org.example.contentservice.config;

import feign.RequestTemplate;
import org.example.cloudcommon.InternalServiceHeaders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InternalFeignRequestInterceptorTest {
    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void addsServiceTokenAndCallerAuthorization() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(InternalServiceHeaders.AUTHORIZATION, "Bearer user-jwt");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        RequestTemplate template = new RequestTemplate();

        new InternalFeignRequestInterceptor("secret").apply(template);

        assertEquals("secret", template.headers().get(InternalServiceHeaders.INTERNAL_TOKEN)
                .iterator().next());
        assertEquals("Bearer user-jwt", template.headers().get(InternalServiceHeaders.AUTHORIZATION)
                .iterator().next());
    }
}
