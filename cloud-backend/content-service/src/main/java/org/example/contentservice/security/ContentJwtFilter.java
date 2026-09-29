package org.example.contentservice.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ContentJwtFilter extends OncePerRequestFilter {
    @Resource
    private ContentJwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        DecodedJWT jwt = jwtUtils.resolve(request.getHeader("Authorization"));
        if (jwt != null) {
            UserDetails user = User.withUsername(jwtUtils.username(jwt)).password("*****").roles("user").build();
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
            request.setAttribute("id", jwtUtils.userId(jwt));
        }
        chain.doFilter(request, response);
    }
}
