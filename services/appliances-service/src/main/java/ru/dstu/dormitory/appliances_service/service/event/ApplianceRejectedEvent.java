package ru.dstu.dormitory.appliances_service.service.event;

import java.util.UUID;

public record ApplianceRejectedEvent(
        UUID applianceId,
        UUID residentId,
        String reason,
        UUID decisionBy
) {
}
