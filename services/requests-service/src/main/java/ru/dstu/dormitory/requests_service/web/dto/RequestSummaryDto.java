package ru.dstu.dormitory.requests_service.web.dto;

import ru.dstu.dormitory.requests_service.domain.enums.RequestStatus;
import ru.dstu.dormitory.requests_service.domain.enums.RequestType;
import ru.dstu.dormitory.requests_service.domain.enums.TargetPool;

import java.time.Instant;
import java.util.UUID;

public record RequestSummaryDto(
        UUID id,
        UUID authorId,
        UUID roomId,
        RequestType type,
        String title,
        RequestStatus status,
        TargetPool targetPool,
        UUID assigneeId,
        Instant scheduledAt,
        Instant createdAt,
        Instant updatedAt
) {
}
