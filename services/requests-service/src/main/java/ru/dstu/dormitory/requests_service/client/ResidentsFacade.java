package ru.dstu.dormitory.requests_service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import ru.dstu.dormitory.requests_service.client.dto.ResidentDto;
import ru.dstu.dormitory.requests_service.client.dto.RoomDto;
import ru.dstu.dormitory.requests_service.exception.ResidentNotEnrolledException;
import ru.dstu.dormitory.requests_service.exception.ResidentsServiceUnavailableException;
import ru.dstu.dormitory.requests_service.util.LogPatterns;

import java.util.UUID;

/**
 * Фасад над {@link ResidentsServiceClient} с кэшированием и явной обработкой недоступности.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResidentsFacade {

    private final ResidentsServiceClient client;

    @Cacheable(value = "resident-by-user", key = "#userId", unless = "#result == null")
    public ResidentDto findResidentByUser(UUID userId) {
        try {
            ResidentDto dto = client.getResidentByUser(userId);
            log.info(LogPatterns.RESIDENTS_CLIENT_CALL, "/residents/by-user/" + userId,
                    dto == null ? "null" : "ok");
            return dto;
        } catch (ResidentNotEnrolledException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn(LogPatterns.RESIDENTS_CLIENT_FALLBACK, ex.getMessage());
            return null;
        }
    }

    public ResidentDto requireResidentByUser(UUID userId) {
        ResidentDto dto = findResidentByUser(userId);
        if (dto == null) {
            throw new ResidentsServiceUnavailableException(
                    "residents-service недоступен: userId=" + userId);
        }
        return dto;
    }

    @Cacheable(value = "room", key = "#roomId", unless = "#result == null")
    public RoomDto findRoom(UUID roomId) {
        try {
            RoomDto dto = client.getRoom(roomId);
            log.info(LogPatterns.RESIDENTS_CLIENT_CALL, "/rooms/" + roomId,
                    dto == null ? "null" : "ok");
            return dto;
        } catch (ResidentNotEnrolledException ex) {
            return null;
        } catch (Exception ex) {
            log.warn(LogPatterns.RESIDENTS_CLIENT_FALLBACK, ex.getMessage());
            return null;
        }
    }
}
