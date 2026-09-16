package ru.dstu.dormitory.requests_service.service.event;

import ru.dstu.dormitory.requests_service.domain.enums.RequestType;

import java.time.Instant;
import java.util.UUID;

public record RequestAssignedEvent(
        UUID requestId,
        UUID assigneeId,
        Instant scheduledAt,
        RequestType type
) {
}
