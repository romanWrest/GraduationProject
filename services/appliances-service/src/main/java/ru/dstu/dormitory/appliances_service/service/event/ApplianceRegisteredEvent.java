package ru.dstu.dormitory.appliances_service.service.event;

import ru.dstu.dormitory.appliances_service.domain.enums.ApplianceType;

import java.util.UUID;

public record ApplianceRegisteredEvent(
        UUID applianceId,
        UUID residentId,
        UUID userId,
        UUID roomId,
        ApplianceType type,
        int powerWatts
) {
}
