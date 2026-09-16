package ru.dstu.dormitory.appliances_service.web.dto.response;

import ru.dstu.dormitory.appliances_service.domain.enums.ApplianceStatus;
import ru.dstu.dormitory.appliances_service.domain.enums.ApplianceType;

import java.time.Instant;
import java.util.UUID;

public record ApplianceDto(
        UUID id,
        UUID residentId,
        UUID userId,
        UUID roomId,
        ApplianceType type,
        String brand,
        String model,
        int powerWatts,
        String photoUrl,
        String notes,
        ApplianceStatus status,
        UUID decisionBy,
        Instant decisionAt,
        String decisionReason,
        Instant createdAt,
        Instant updatedAt
) {
}
