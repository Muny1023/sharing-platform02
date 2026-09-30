package org.example.contentservice.config;

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
    void protectsContentInternalEndpoints() throws Exception {
        InternalServiceTokenFilter filter = new InternalServiceTokenFilter("secret",
                List.of("/internal/content"));
        var chain = mock(jakarta.servlet.FilterChain.class);
        var rejected = new MockHttpServletRequest("POST", "/internal/content/posts/status");
        var rejectedResponse = new MockHttpServletResponse();

        filter.doFilter(rejected, rejectedResponse, chain);

        assertEquals(401, rejectedResponse.getStatus());
        verify(chain, never()).doFilter(rejected, rejectedResponse);

        var accepted = new MockHttpServletRequest("POST", "/internal/content/posts/status");
        accepted.addHeader(InternalServiceHeaders.INTERNAL_TOKEN, "secret");
        var acceptedResponse = new MockHttpServletResponse();
        filter.doFilter(accepted, acceptedResponse, chain);

        verify(chain).doFilter(accepted, acceptedResponse);
    }
}
