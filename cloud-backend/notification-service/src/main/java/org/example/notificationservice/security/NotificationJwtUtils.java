package org.example.notificationservice.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NotificationJwtUtils {
    @Value("${spring.security.jwt.key}")
    private String key;

    public DecodedJWT resolve(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) return null;
        try {
            return JWT.require(Algorithm.HMAC256(key)).build().verify(authorization.substring(7));
        } catch (JWTVerificationException ignored) {
            return null;
        }
    }

    public Integer userId(DecodedJWT jwt) {
        return jwt == null ? null : jwt.getClaim("id").asInt();
    }

    public String username(DecodedJWT jwt) {
        Map<String, Claim> claims = jwt.getClaims();
        return claims.get("name") == null ? "user" : claims.get("name").asString();
    }
}
