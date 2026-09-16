package ru.dstu.dormitory.notifications_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.dstu.dormitory.notifications_service.client.dto.AuthUserDto;
import ru.dstu.dormitory.notifications_service.util.LogPatterns;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class AuthServiceClientFallback implements AuthServiceClient {

    @Override
    public AuthUserDto getUser(UUID id) {
        log.warn(LogPatterns.AUTH_CLIENT_FALLBACK, "getUser(%s)".formatted(id));
        return null;
    }

    @Override
    public List<AuthUserDto> getUsersByRole(String role) {
        log.warn(LogPatterns.AUTH_CLIENT_FALLBACK, "getUsersByRole(%s)".formatted(role));
        return List.of();
    }
}
