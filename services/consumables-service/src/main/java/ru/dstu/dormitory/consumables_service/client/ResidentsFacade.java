package ru.dstu.dormitory.consumables_service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import ru.dstu.dormitory.consumables_service.client.dto.ResidentDto;
import ru.dstu.dormitory.consumables_service.config.RedisConfig;
import ru.dstu.dormitory.consumables_service.exception.ResidentNotEnrolledException;
import ru.dstu.dormitory.consumables_service.exception.ResidentsServiceUnavailableException;
import ru.dstu.dormitory.consumables_service.util.LogPatterns;

import java.util.UUID;

/**
 * Фасад над {@link ResidentsServiceClient} с кешем Redis (TTL 5 мин)
 * и явной обработкой недоступности residents-service.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResidentsFacade {

    private final ResidentsServiceClient client;

    @Cacheable(value = RedisConfig.CACHE_RESIDENT, key = "#residentId", unless = "#result == null")
    public ResidentDto findResident(UUID residentId) {
        try {
            ResidentDto dto = client.getResident(residentId);
            log.info(LogPatterns.RESIDENTS_CLIENT_CALL, "/residents/" + residentId,
                    dto == null ? "null" : "ok");
            return dto;
        } catch (ResidentNotEnrolledException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn(LogPatterns.RESIDENTS_CLIENT_FALLBACK, ex.getMessage());
            return null;
        }
    }

    public ResidentDto requireResident(UUID residentId) {
        ResidentDto dto = findResident(residentId);
        if (dto == null) {
            throw new ResidentsServiceUnavailableException(
                    "residents-service недоступен: residentId=" + residentId);
        }
        return dto;
    }
}
