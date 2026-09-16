package ru.dstu.dormitory.requests_service.service.event;

import ru.dstu.dormitory.requests_service.domain.enums.RequestType;
import ru.dstu.dormitory.requests_service.domain.enums.TargetPool;

import java.time.Instant;
import java.util.UUID;

public record RequestCreatedEvent(
        UUID requestId,
        RequestType type,
        UUID authorId,
        UUID roomId,
        TargetPool targetPool,
        Instant createdAt
) {
}
