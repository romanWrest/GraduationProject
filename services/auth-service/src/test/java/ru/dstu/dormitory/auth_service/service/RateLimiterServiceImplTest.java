package ru.dstu.dormitory.auth_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import ru.dstu.dormitory.auth_service.config.RateLimitProperties;
import ru.dstu.dormitory.auth_service.exception.RateLimitExceededException;
import ru.dstu.dormitory.auth_service.service.Impl.RateLimiterServiceImpl;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimiterServiceImplTest {

    @Mock
    StringRedisTemplate redis;

    @Mock
    ValueOperations<String, String> valueOps;

    RateLimitProperties properties;

    @InjectMocks
    RateLimiterServiceImpl service;

    @BeforeEach
    void setUp() {
        properties = new RateLimitProperties();
        properties.getLogin().setIpMax(3);
        properties.getLogin().setIpWindowSeconds(60);
        properties.getLogin().setEmailMax(3);
        properties.getLogin().setEmailWindowSeconds(300);
        service = new RateLimiterServiceImpl(redis, properties);
        when(redis.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void firstHit_setsExpiry() {
        when(valueOps.increment(anyString())).thenReturn(1L);

        assertThatCode(() -> service.checkLogin("1.2.3.4", "u@e.com"))
                .doesNotThrowAnyException();

        verify(redis, times(2)).expire(anyString(), any(Duration.class));
    }

    @Test
    void overLimit_throwsWithRetryAfter() {
        when(valueOps.increment(anyString())).thenReturn(99L);
        when(redis.getExpire(anyString())).thenReturn(42L);

        assertThatThrownBy(() -> service.checkLogin("1.2.3.4", null))
                .isInstanceOf(RateLimitExceededException.class)
                .satisfies(ex -> assertThat(((RateLimitExceededException) ex).getRetryAfterSeconds()).isEqualTo(42L));
    }

    @Test
    void missingIp_onlyEmailChecked() {
        when(valueOps.increment(anyString())).thenReturn(1L);

        service.checkLogin(null, "u@e.com");

        verify(valueOps, times(1)).increment(anyString());
    }
}
