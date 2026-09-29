package org.example.accountservice.filter;

import org.example.accountservice.util.AccountConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlowLimitFilterTest {
    @Mock
    private StringRedisTemplate template;

    @InjectMocks
    private FlowLimitFilter filter;

    @Test
    void onlyAuthRequestsAreCountedAndBlockedAfterLimit() throws Exception {
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> operations = mock(ValueOperations.class);
        AtomicLong count = new AtomicLong();
        when(template.opsForValue()).thenReturn(operations);
        when(template.hasKey(contains(AccountConstants.FLOW_LIMIT_COUNTER)))
                .thenAnswer(invocation -> count.get() > 0);
        when(template.hasKey(contains(AccountConstants.FLOW_LIMIT_BLOCK))).thenReturn(false);
        when(operations.increment(contains(AccountConstants.FLOW_LIMIT_COUNTER)))
                .thenAnswer(invocation -> count.incrementAndGet());
        doAnswer(invocation -> {
            count.set(1);
            return null;
        }).when(operations).set(contains(AccountConstants.FLOW_LIMIT_COUNTER), anyString(), anyLong(), any(TimeUnit.class));

        MockHttpServletRequest normal = new MockHttpServletRequest("GET", "/api/user/me");
        normal.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        var chain = mock(jakarta.servlet.FilterChain.class);
        filter.doFilter(normal, response, chain);
        assertEquals(0, count.get());

        MockHttpServletRequest auth = new MockHttpServletRequest("POST", "/api/auth/login");
        auth.setRemoteAddr("127.0.0.1");
        for (int i = 0; i < FlowLimitFilter.MAX_REQUESTS_PER_WINDOW; i++) {
            filter.doFilter(auth, response, chain);
        }
        filter.doFilter(auth, response, chain);

        assertEquals(403, response.getStatus());
        verify(chain, times(1 + FlowLimitFilter.MAX_REQUESTS_PER_WINDOW)).doFilter(any(), any());
    }
}
