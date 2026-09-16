package ru.dstu.dormitory.requests_service.web.dto;

import ru.dstu.dormitory.requests_service.domain.enums.RequestStatus;
import ru.dstu.dormitory.requests_service.domain.enums.RequestType;
import ru.dstu.dormitory.requests_service.domain.enums.TargetPool;

import java.time.Instant;
import java.util.UUID;

public record RequestDto(
        UUID id,
        UUID authorId,
        UUID authorResidentId,
        UUID roomId,
        RequestType type,
        String title,
        String description,
        String reason,
        RequestStatus status,
        TargetPool targetPool,
        UUID assigneeId,
        Instant scheduledAt,
        String resolutionComment,
        String rejectionReason,
        String cancellationReason,
        String reopenReason,
        boolean autoClosed,
        Instant createdAt,
        Instant updatedAt,
        Instant closedAt
) {
}
