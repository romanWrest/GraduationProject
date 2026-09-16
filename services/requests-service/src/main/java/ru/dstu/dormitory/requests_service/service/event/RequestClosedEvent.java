package ru.dstu.dormitory.requests_service.service.event;

import ru.dstu.dormitory.requests_service.domain.enums.RequestType;

import java.time.Instant;
import java.util.UUID;

public record RequestClosedEvent(
        UUID requestId,
        boolean autoClosed,
        Instant closedAt,
        RequestType type
) {
}
