package ru.dstu.dormitory.auth_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import ru.dstu.dormitory.auth_service.config.JwtProperties;
import ru.dstu.dormitory.auth_service.exception.RefreshTokenInvalidException;
import ru.dstu.dormitory.auth_service.exception.RefreshTokenReuseException;
import ru.dstu.dormitory.auth_service.service.Impl.RefreshTokenServiceImpl;
import ru.dstu.dormitory.auth_service.util.HashUtil;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock
    StringRedisTemplate redis;

    @Mock
    ValueOperations<String, String> valueOps;

    @Mock
    SetOperations<String, String> setOps;

    JwtProperties jwtProperties;

    RefreshTokenServiceImpl service;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret("0123456789-0123456789-0123456789-secret");
        jwtProperties.setAccessTtl(Duration.ofMinutes(15));
        jwtProperties.setRefreshTtl(Duration.ofDays(7));
        service = new RefreshTokenServiceImpl(redis, jwtProperties);
    }

    @Test
    void issueNew_storesTokenAndFamily() {
        when(redis.opsForValue()).thenReturn(valueOps);
        when(redis.opsForSet()).thenReturn(setOps);

        UUID userId = UUID.randomUUID();
        RefreshTokenService.IssuedToken issued = service.issueNew(userId);

        assertThat(issued.rawToken()).isNotBlank();
        assertThat(issued.familyId()).isNotNull();
        verify(valueOps).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void rotate_missingToken_throwsInvalid() {
        when(redis.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(anyString())).thenReturn(null);
        when(redis.hasKey(anyString())).thenReturn(false);

        assertThatThrownBy(() -> service.rotate(UUID.randomUUID().toString()))
                .isInstanceOf(RefreshTokenInvalidException.class);
    }

    @Test
    void rotate_reusedToken_invalidatesFamilyAndThrows() {
        UUID userId = UUID.randomUUID();
        UUID familyId = UUID.randomUUID();
        String rawToken = UUID.randomUUID().toString();
        String payload = "%s|%s".formatted(userId, familyId);
        String tokenHash = HashUtil.sha256Hex(rawToken);

        when(redis.opsForValue()).thenReturn(valueOps);
        when(redis.opsForSet()).thenReturn(setOps);
        when(valueOps.get("auth:refresh:%s".formatted(tokenHash))).thenReturn(null);
        when(redis.hasKey("auth:refresh:used:%s".formatted(tokenHash))).thenReturn(true);
        when(valueOps.get("auth:refresh:used:%s".formatted(tokenHash))).thenReturn(payload);
        when(setOps.members("auth:refresh:family:%s".formatted(familyId)))
                .thenReturn(Set.of("otherHash"));

        assertThatThrownBy(() -> service.rotate(rawToken))
                .isInstanceOf(RefreshTokenReuseException.class);

        verify(redis).delete("auth:refresh:otherHash");
        verify(redis).delete("auth:refresh:family:%s".formatted(familyId));
    }

    @Test
    void rotate_validToken_issuesNewAndMarksUsed() {
        UUID userId = UUID.randomUUID();
        UUID familyId = UUID.randomUUID();
        String rawToken = UUID.randomUUID().toString();
        String tokenHash = HashUtil.sha256Hex(rawToken);
        String payload = "%s|%s".formatted(userId, familyId);

        when(redis.opsForValue()).thenReturn(valueOps);
        when(redis.opsForSet()).thenReturn(setOps);
        when(valueOps.get("auth:refresh:%s".formatted(tokenHash))).thenReturn(payload);

        RefreshTokenService.RotatedToken rotated = service.rotate(rawToken);

        assertThat(rotated.userId()).isEqualTo(userId);
        assertThat(rotated.familyId()).isEqualTo(familyId);
        assertThat(rotated.newRawToken()).isNotBlank();
        verify(valueOps).set(eq("auth:refresh:used:%s".formatted(tokenHash)), eq(payload), any(Duration.class));
    }

    @Test
    void revoke_unknownToken_isNoop() {
        when(redis.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(anyString())).thenReturn(null);

        service.revoke(UUID.randomUUID().toString());

        verify(redis, org.mockito.Mockito.never()).delete(anyString());
    }

    @Test
    void revokeAllForUser_removesAllFamilies() {
        UUID userId = UUID.randomUUID();
        UUID familyId = UUID.randomUUID();

        when(redis.opsForSet()).thenReturn(setOps);
        when(setOps.members("auth:refresh:user:%s".formatted(userId)))
                .thenReturn(Set.of(familyId.toString()));

        service.revokeAllForUser(userId);

        verify(redis).delete("auth:refresh:family:%s".formatted(familyId));
        verify(redis).delete("auth:refresh:user:%s".formatted(userId));
    }
}
