package ru.dstu.dormitory.requests_service.web.dto;

import ru.dstu.dormitory.requests_service.domain.enums.RequestStatus;

import java.time.Instant;
import java.util.UUID;

public record StatusHistoryDto(
        UUID id,
        UUID requestId,
        RequestStatus fromStatus,
        RequestStatus toStatus,
        UUID actorId,
        String comment,
        Instant changedAt
) {
}
