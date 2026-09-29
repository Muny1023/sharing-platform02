package org.example.accountservice.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtils {
    @Value("${spring.security.jwt.key}")
    private String key;

    @Value("${spring.security.jwt.expire}")
    private int expire;

    @Resource
    private StringRedisTemplate template;

    public String createJwt(UserDetails details, int id, String username) {
        Algorithm algorithm = Algorithm.HMAC256(key);
        Date expireAt = expireTime();
        return JWT.create()
                .withJWTId(UUID.randomUUID().toString())
                .withClaim("id", id)
                .withClaim("name", username)
                .withClaim("authorities", details.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority).toList())
                .withExpiresAt(expireAt)
                .withIssuedAt(new Date())
                .sign(algorithm);
    }

    public Date expireTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, expire * 24);
        return calendar.getTime();
    }

    public DecodedJWT resolveJwt(String headerToken) {
        String token = convertToken(headerToken);
        if (token == null) {
            return null;
        }
        try {
            DecodedJWT jwt = JWT.require(Algorithm.HMAC256(key)).build().verify(token);
            if (Boolean.TRUE.equals(template.hasKey(AccountConstants.JWT_BLACK_LIST + jwt.getId()))) {
                return null;
            }
            return jwt.getExpiresAt() != null && new Date().before(jwt.getExpiresAt()) ? jwt : null;
        } catch (JWTVerificationException exception) {
            return null;
        }
    }

    public boolean invalidateJwt(String headerToken) {
        String token = convertToken(headerToken);
        if (token == null) {
            return false;
        }
        try {
            DecodedJWT jwt = JWT.require(Algorithm.HMAC256(key)).build().verify(token);
            String key = AccountConstants.JWT_BLACK_LIST + jwt.getId();
            if (Boolean.TRUE.equals(template.hasKey(key))) {
                return false;
            }
            long ttl = Math.max(jwt.getExpiresAt().getTime() - System.currentTimeMillis(), 0);
            template.opsForValue().set(key, "", ttl, TimeUnit.MILLISECONDS);
            return true;
        } catch (JWTVerificationException exception) {
            return false;
        }
    }

    public UserDetails toUser(DecodedJWT jwt) {
        Map<String, Claim> claims = jwt.getClaims();
        return User.withUsername(claims.get("name").asString())
                .password("*****")
                .authorities(claims.get("authorities").asArray(String.class))
                .build();
    }

    public Integer toId(DecodedJWT jwt) {
        return jwt.getClaim("id").asInt();
    }

    private String convertToken(String headerToken) {
        return headerToken != null && headerToken.startsWith("Bearer ")
                ? headerToken.substring(7) : null;
    }
}
