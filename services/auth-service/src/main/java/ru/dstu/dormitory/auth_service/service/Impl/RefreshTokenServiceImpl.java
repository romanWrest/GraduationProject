package ru.dstu.dormitory.auth_service.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import ru.dstu.dormitory.auth_service.aspect.annotation.LogMethod;
import ru.dstu.dormitory.auth_service.config.JwtProperties;
import ru.dstu.dormitory.auth_service.exception.RefreshTokenInvalidException;
import ru.dstu.dormitory.auth_service.exception.RefreshTokenReuseException;
import ru.dstu.dormitory.auth_service.service.RefreshTokenService;
import ru.dstu.dormitory.auth_service.util.HashUtil;
import ru.dstu.dormitory.auth_service.util.LogPatterns;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final String TOKEN_KEY = "auth:refresh:%s";
    private static final String FAMILY_KEY = "auth:refresh:family:%s";
    private static final String USED_KEY = "auth:refresh:used:%s";
    private static final String USER_FAMILIES_KEY = "auth:refresh:user:%s";

    private final StringRedisTemplate redis;
    private final JwtProperties jwtProperties;

    @Override
    @LogMethod(value = "Выдача нового refresh-токена", logArgs = {"userId"})
    public IssuedToken issueNew(UUID userId) {
        UUID familyId = UUID.randomUUID();
        String rawToken = UUID.randomUUID().toString();
        storeToken(rawToken, userId, familyId);
        log.info(LogPatterns.REFRESH_ISSUED, userId, familyId);
        return new IssuedToken(rawToken, familyId);
    }

    @Override
    @LogMethod(value = "Ротация refresh-токена", maskArgs = {"rawRefreshToken"})
    public RotatedToken rotate(String rawRefreshToken) {
        String tokenHash = HashUtil.sha256Hex(rawRefreshToken);
        String tokenKey = TOKEN_KEY.formatted(tokenHash);
        String usedKey = USED_KEY.formatted(tokenHash);

        String payload = redis.opsForValue().get(tokenKey);

        if (payload == null) {
            if (Boolean.TRUE.equals(redis.hasKey(usedKey))) {
                String usedPayload = redis.opsForValue().get(usedKey);
                handleReuse(usedPayload);
                throw new RefreshTokenReuseException("token family compromised");
            }
            throw new RefreshTokenInvalidException("Refresh токен недействителен");
        }

        TokenRecord record = TokenRecord.parse(payload);
        Duration ttl = jwtProperties.getRefreshTtl();

        redis.delete(tokenKey);
        redis.opsForValue().set(usedKey, payload, ttl);
        redis.opsForSet().remove(FAMILY_KEY.formatted(record.familyId()), tokenHash);

        String newRaw = UUID.randomUUID().toString();
        storeTokenInFamily(newRaw, record.userId(), record.familyId());
        log.info(LogPatterns.REFRESH_ISSUED, record.userId(), record.familyId());
        return new RotatedToken(record.userId(), newRaw, record.familyId());
    }

    @Override
    @LogMethod(value = "Отзыв refresh-токена", maskArgs = {"rawRefreshToken"})
    public void revoke(String rawRefreshToken) {
        String tokenHash = HashUtil.sha256Hex(rawRefreshToken);
        String tokenKey = TOKEN_KEY.formatted(tokenHash);
        String payload = redis.opsForValue().get(tokenKey);
        if (payload == null) {
            return;
        }
        TokenRecord record = TokenRecord.parse(payload);
        redis.delete(tokenKey);
        redis.opsForSet().remove(FAMILY_KEY.formatted(record.familyId()), tokenHash);
        log.info(LogPatterns.REFRESH_REVOKED, tokenHash);
    }

    @Override
    @LogMethod(value = "Отзыв всех refresh-токенов пользователя", logArgs = {"userId"})
    public void revokeAllForUser(UUID userId) {
        String userFamiliesKey = USER_FAMILIES_KEY.formatted(userId);
        Set<String> families = redis.opsForSet().members(userFamiliesKey);
        if (families != null) {
            for (String familyId : families) {
                invalidateFamily(UUID.fromString(familyId));
            }
        }
        redis.delete(userFamiliesKey);
    }

    private void storeToken(String rawToken, UUID userId, UUID familyId) {
        String tokenHash = HashUtil.sha256Hex(rawToken);
        Duration ttl = jwtProperties.getRefreshTtl();
        String payload = TokenRecord.of(userId, familyId).serialize();

        redis.opsForValue().set(TOKEN_KEY.formatted(tokenHash), payload, ttl);
        redis.opsForSet().add(FAMILY_KEY.formatted(familyId), tokenHash);
        redis.expire(FAMILY_KEY.formatted(familyId), ttl);
        redis.opsForSet().add(USER_FAMILIES_KEY.formatted(userId), familyId.toString());
        redis.expire(USER_FAMILIES_KEY.formatted(userId), ttl);
    }

    private void storeTokenInFamily(String rawToken, UUID userId, UUID familyId) {
        String tokenHash = HashUtil.sha256Hex(rawToken);
        Duration ttl = jwtProperties.getRefreshTtl();
        String payload = TokenRecord.of(userId, familyId).serialize();

        redis.opsForValue().set(TOKEN_KEY.formatted(tokenHash), payload, ttl);
        redis.opsForSet().add(FAMILY_KEY.formatted(familyId), tokenHash);
        redis.expire(FAMILY_KEY.formatted(familyId), ttl);
    }

    private void handleReuse(String usedPayload) {
        if (usedPayload == null) {
            return;
        }
        TokenRecord record = TokenRecord.parse(usedPayload);
        invalidateFamily(record.familyId());
        log.warn(LogPatterns.REFRESH_REUSE_DETECTED, record.userId(), record.familyId());
    }

    private void invalidateFamily(UUID familyId) {
        String familyKey = FAMILY_KEY.formatted(familyId);
        Set<String> members = redis.opsForSet().members(familyKey);
        if (members != null) {
            for (String hash : members) {
                redis.delete(TOKEN_KEY.formatted(hash));
            }
        }
        redis.delete(familyKey);
    }

    private record TokenRecord(UUID userId, UUID familyId) {

        static TokenRecord of(UUID userId, UUID familyId) {
            return new TokenRecord(userId, familyId);
        }

        String serialize() {
            return "%s|%s".formatted(userId, familyId);
        }

        static TokenRecord parse(String payload) {
            String[] parts = payload.split("\\|");
            if (parts.length != 2) {
                throw new RefreshTokenInvalidException("Повреждённая запись refresh-токена");
            }
            return new TokenRecord(UUID.fromString(parts[0]), UUID.fromString(parts[1]));
        }
    }
}
