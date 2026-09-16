package ru.dstu.dormitory.notifications_service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import ru.dstu.dormitory.notifications_service.aspect.annotation.LogMethod;
import ru.dstu.dormitory.notifications_service.client.dto.AuthUserDto;
import ru.dstu.dormitory.notifications_service.config.RedisConfig;
import ru.dstu.dormitory.notifications_service.util.LogPatterns;

import java.util.List;
import java.util.UUID;

/**
 * Кэширующая обёртка над {@link AuthServiceClient}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthFacade {

    private final AuthServiceClient client;

    /** Загрузить пользователя из auth по id. Кэш 5 минут. {@code null} — если auth недоступен. */
    @LogMethod(value = "auth: getUser", logArgs = {"userId"})
    @Cacheable(value = RedisConfig.CACHE_USER, key = "#userId", unless = "#result == null")
    public AuthUserDto getUser(UUID userId) {
        log.debug(LogPatterns.AUTH_CLIENT_CALL, "/users/" + userId, "request");
        return client.getUser(userId);
    }

    /** Список пользователей с заданной ролью. Кэш 1 минута. */
    @LogMethod(value = "auth: getUsersByRole", logArgs = {"role"})
    @Cacheable(value = RedisConfig.CACHE_USERS_BY_ROLE, key = "#role")
    public List<AuthUserDto> getUsersByRole(String role) {
        log.debug(LogPatterns.AUTH_CLIENT_CALL, "/users?role=" + role, "request");
        List<AuthUserDto> result = client.getUsersByRole(role);
        return result == null ? List.of() : result;
    }
}
