package ru.dstu.dormitory.auth_service.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import ru.dstu.dormitory.auth_service.config.RateLimitProperties;
import ru.dstu.dormitory.auth_service.exception.RateLimitExceededException;
import ru.dstu.dormitory.auth_service.service.RateLimiterService;
import ru.dstu.dormitory.auth_service.util.LogPatterns;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimiterServiceImpl implements RateLimiterService {

    private static final String IP_KEY = "auth:ratelimit:login:ip:%s";
    private static final String EMAIL_KEY = "auth:ratelimit:login:email:%s";

    private final StringRedisTemplate redis;
    private final RateLimitProperties properties;

    @Override
    public void checkLogin(String ip, String email) {
        RateLimitProperties.Login cfg = properties.getLogin();
        if (ip != null && !ip.isBlank()) {
            hitOrThrow(IP_KEY.formatted(ip), cfg.getIpMax(), Duration.ofSeconds(cfg.getIpWindowSeconds()));
        }
        if (email != null && !email.isBlank()) {
            hitOrThrow(EMAIL_KEY.formatted(email.toLowerCase()), cfg.getEmailMax(),
                    Duration.ofSeconds(cfg.getEmailWindowSeconds()));
        }
    }

    private void hitOrThrow(String key, int max, Duration window) {
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redis.expire(key, window);
        }
        if (count != null && count > max) {
            Long ttl = redis.getExpire(key);
            long retryAfter = ttl != null && ttl > 0 ? ttl : window.toSeconds();
            log.warn(LogPatterns.RATE_LIMIT_HIT, key, max);
            throw new RateLimitExceededException(
                    "Превышен лимит попыток: %d за %ds".formatted(max, window.toSeconds()),
                    retryAfter);
        }
    }
}
