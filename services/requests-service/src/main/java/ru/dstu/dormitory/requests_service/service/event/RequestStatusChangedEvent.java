package ru.dstu.dormitory.requests_service.service.event;

import ru.dstu.dormitory.requests_service.domain.enums.RequestStatus;
import ru.dstu.dormitory.requests_service.domain.enums.RequestType;

import java.time.Instant;
import java.util.UUID;

public record RequestStatusChangedEvent(
        UUID requestId,
        RequestStatus fromStatus,
        RequestStatus toStatus,
        UUID actorId,
        Instant changedAt,
        RequestType type
) {
}
