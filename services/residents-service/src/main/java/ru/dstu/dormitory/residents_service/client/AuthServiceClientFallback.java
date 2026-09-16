package ru.dstu.dormitory.residents_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.dstu.dormitory.residents_service.client.dto.AuthUserDto;
import ru.dstu.dormitory.residents_service.client.dto.BatchUserIdsRequest;
import ru.dstu.dormitory.residents_service.util.LogPatterns;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class AuthServiceClientFallback implements AuthServiceClient {

    @Override
    public AuthUserDto getUser(UUID id) {
        log.warn(LogPatterns.AUTH_SERVICE_UNAVAILABLE, "getUser(%s)".formatted(id));
        return null;
    }

    @Override
    public List<AuthUserDto> getUsersBatch(BatchUserIdsRequest request) {
        log.warn(LogPatterns.AUTH_SERVICE_UNAVAILABLE, "getUsersBatch(%d ids)".formatted(
                request == null || request.ids() == null ? 0 : request.ids().size()));
        return List.of();
    }
}
