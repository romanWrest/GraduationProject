package ru.dstu.dormitory.appliances_service.service.event;

import java.util.UUID;

public record ApplianceRevokedEvent(
        UUID applianceId,
        UUID residentId,
        String reason,
        UUID decisionBy,
        boolean autoRevoked
) {
}
