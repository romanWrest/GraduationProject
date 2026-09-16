package ru.dstu.dormitory.appliances_service.service.event;

import java.time.Instant;
import java.util.UUID;

public record ApplianceApprovedEvent(
        UUID applianceId,
        UUID residentId,
        UUID decisionBy,
        Instant decisionAt
) {
}
