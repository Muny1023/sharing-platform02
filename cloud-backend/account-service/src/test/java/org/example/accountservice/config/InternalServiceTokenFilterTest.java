package org.example.accountservice.config;

import jakarta.servlet.FilterChain;
import org.example.cloudcommon.InternalServiceHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

class InternalServiceTokenFilterTest {
    @Test
    void rejectsInternalRequestWithoutToken() throws Exception {
        InternalServiceTokenFilter filter = new InternalServiceTokenFilter("secret",
                List.of("/internal/accounts"));
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/internal/accounts/batch");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void acceptsInternalRequestWithToken() throws Exception {
        InternalServiceTokenFilter filter = new InternalServiceTokenFilter("secret",
                List.of("/internal/accounts"));
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/internal/accounts/batch");
        request.addHeader(InternalServiceHeaders.INTERNAL_TOKEN, "secret");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }
}
