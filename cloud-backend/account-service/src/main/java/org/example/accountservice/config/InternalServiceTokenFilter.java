package org.example.accountservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.cloudcommon.InternalServiceHeaders;
import org.example.cloudcommon.RestBean;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

final class InternalServiceTokenFilter extends OncePerRequestFilter {
    private final String expectedToken;
    private final List<String> protectedPrefixes;

    InternalServiceTokenFilter(String expectedToken, List<String> protectedPrefixes) {
        this.expectedToken = expectedToken;
        this.protectedPrefixes = List.copyOf(protectedPrefixes);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return protectedPrefixes.stream().noneMatch(prefix ->
                path.equals(prefix) || path.startsWith(prefix + "/"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!InternalServiceHeaders.hasValidToken(expectedToken,
                request.getHeader(InternalServiceHeaders.INTERNAL_TOKEN))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write(RestBean.unauthorized("内部服务令牌无效").asJsonString());
            return;
        }
        filterChain.doFilter(request, response);
    }
}
