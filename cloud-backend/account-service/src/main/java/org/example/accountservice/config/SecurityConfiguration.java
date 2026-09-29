package org.example.accountservice.config;

import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.accountservice.entity.Account;
import org.example.accountservice.entity.response.AuthorizeResponse;
import org.example.accountservice.filter.JwtAuthorizeFilter;
import org.example.accountservice.service.AccountService;
import org.example.accountservice.util.JwtUtils;
import org.example.cloudcommon.RestBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Configuration
public class SecurityConfiguration {
    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private JwtAuthorizeFilter jwtAuthorizeFilter;

    @Resource
    private AccountService accountService;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(conf -> conf
                        .requestMatchers("/api/auth/**", "/internal/ai/**", "/error").permitAll()
                        .anyRequest().authenticated())
                .formLogin(conf -> conf
                        .loginProcessingUrl("/api/auth/login")
                        .successHandler(this::onAuthenticationSuccess)
                        .failureHandler(this::onAuthenticationFailure))
                .logout(conf -> conf
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(this::onLogoutSuccess))
                .exceptionHandling(conf -> conf
                        .authenticationEntryPoint(this::onUnauthorized)
                        .accessDeniedHandler(this::onAccessDenied))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(conf -> conf.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthorizeFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    private void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        User user = (User) authentication.getPrincipal();
        Account account = accountService.findAccountByNameOrEmail(user.getUsername());
        AuthorizeResponse result = new AuthorizeResponse();
        result.setExpire(jwtUtils.expireTime());
        result.setRoles(account.getRole());
        result.setToken(jwtUtils.createJwt(user, account.getId(), account.getUsername()));
        result.setUsername(account.getUsername());
        response.getWriter().write(RestBean.success(result).asJsonString());
    }

    private void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                         AuthenticationException exception) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        write(response, RestBean.unauthorized(exception.getMessage()));
    }

    private void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                 Authentication authentication) throws IOException, ServletException {
        if (jwtUtils.invalidateJwt(request.getHeader("Authorization"))) {
            write(response, RestBean.success());
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            write(response, RestBean.failure(400, "退出登录失败"));
        }
    }

    private void onUnauthorized(HttpServletRequest request, HttpServletResponse response,
                                AuthenticationException exception) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        write(response, RestBean.unauthorized(exception.getMessage()));
    }

    private void onAccessDenied(HttpServletRequest request, HttpServletResponse response,
                                AccessDeniedException exception) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        write(response, RestBean.forbidden(exception.getMessage()));
    }

    private void write(HttpServletResponse response, RestBean<?> body) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(body.asJsonString());
    }
}
