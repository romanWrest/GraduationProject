package ru.dstu.dormitory.requests_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.dstu.dormitory.requests_service.client.dto.AuthUserDto;
import ru.dstu.dormitory.requests_service.util.LogPatterns;

import java.util.UUID;

@Slf4j
@Component
public class AuthServiceClientFallback implements AuthServiceClient {

    @Override
    public AuthUserDto getUser(UUID id) {
        log.warn(LogPatterns.AUTH_CLIENT_FALLBACK, "getUser(%s)".formatted(id));
        return null;
    }
}
