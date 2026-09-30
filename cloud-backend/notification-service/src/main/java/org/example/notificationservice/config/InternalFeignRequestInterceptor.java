package org.example.notificationservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.example.cloudcommon.InternalServiceHeaders;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

final class InternalFeignRequestInterceptor implements RequestInterceptor {
    private final String internalToken;

    InternalFeignRequestInterceptor(String internalToken) {
        this.internalToken = internalToken;
    }

    @Override
    public void apply(RequestTemplate template) {
        if (internalToken != null && !internalToken.isBlank()) {
            template.header(InternalServiceHeaders.INTERNAL_TOKEN, internalToken);
        }
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            String authorization = servletAttributes.getRequest()
                    .getHeader(InternalServiceHeaders.AUTHORIZATION);
            if (authorization != null && !authorization.isBlank()) {
                template.header(InternalServiceHeaders.AUTHORIZATION, authorization);
            }
        }
    }
}
