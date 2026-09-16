package ru.dstu.dormitory.requests_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.dstu.dormitory.requests_service.client.dto.ResidentDto;
import ru.dstu.dormitory.requests_service.client.dto.RoomDto;
import ru.dstu.dormitory.requests_service.exception.ResidentNotEnrolledException;
import ru.dstu.dormitory.requests_service.util.LogPatterns;

import java.util.UUID;

@Slf4j
@Component
public class ResidentsServiceClientFallbackFactory implements FallbackFactory<ResidentsServiceClient> {

    @Override
    public ResidentsServiceClient create(Throwable cause) {
        ResidentNotEnrolledException notEnrolled = findInChain(cause, ResidentNotEnrolledException.class);
        return new ResidentsServiceClient() {
            @Override
            public ResidentDto getResidentByUser(UUID userId) {
                if (notEnrolled != null) {
                    throw notEnrolled;
                }
                log.warn(LogPatterns.RESIDENTS_CLIENT_FALLBACK,
                        "getResidentByUser(%s): %s".formatted(userId, cause.getMessage()));
                return null;
            }

            @Override
            public RoomDto getRoom(UUID id) {
                if (notEnrolled != null) {
                    return null;
                }
                log.warn(LogPatterns.RESIDENTS_CLIENT_FALLBACK,
                        "getRoom(%s): %s".formatted(id, cause.getMessage()));
                return null;
            }
        };
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> T findInChain(Throwable throwable, Class<T> type) {
        Throwable cursor = throwable;
        while (cursor != null) {
            if (type.isInstance(cursor)) {
                return (T) cursor;
            }
            if (cursor.getCause() == cursor) {
                return null;
            }
            cursor = cursor.getCause();
        }
        return null;
    }
}
