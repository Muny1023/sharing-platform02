package org.example.accountservice.filter;

import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.accountservice.util.AccountConstants;
import org.example.cloudcommon.RestBean;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@Order(AccountConstants.ORDER_LIMIT)
public class FlowLimitFilter extends HttpFilter {
    public static final int MAX_REQUESTS_PER_WINDOW = 10;
    private static final String AUTH_PREFIX = "/api/auth/";

    @Resource
    private StringRedisTemplate template;

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!request.getRequestURI().startsWith(AUTH_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }
        String address = request.getRemoteAddr();
        if (tryCount(address)) {
            chain.doFilter(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write(RestBean.forbidden("操作频繁，请稍后再试").asJsonString());
        }
    }

    private boolean tryCount(String ip) {
        synchronized (ip.intern()) {
            if (Boolean.TRUE.equals(template.hasKey(AccountConstants.FLOW_LIMIT_BLOCK + ip))) return false;
            String counter = AccountConstants.FLOW_LIMIT_COUNTER + ip;
            if (Boolean.TRUE.equals(template.hasKey(counter))) {
                long increment = Optional.ofNullable(template.opsForValue().increment(counter)).orElse(0L);
                if (increment > MAX_REQUESTS_PER_WINDOW) {
                    template.opsForValue().set(AccountConstants.FLOW_LIMIT_BLOCK + ip, "", 30, TimeUnit.SECONDS);
                    return false;
                }
            } else {
                template.opsForValue().set(counter, "1", 3, TimeUnit.SECONDS);
            }
            return true;
        }
    }
}
