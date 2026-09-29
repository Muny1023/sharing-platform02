package org.example.accountservice.util;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class FlowUtils {
    @Resource
    private StringRedisTemplate template;

    public boolean limitOnceCheck(String key, int blockTime) {
        if (Boolean.TRUE.equals(template.hasKey(key))) {
            return false;
        }
        template.opsForValue().set(key, "", blockTime, TimeUnit.SECONDS);
        return true;
    }
}
