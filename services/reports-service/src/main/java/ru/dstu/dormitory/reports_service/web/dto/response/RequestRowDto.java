package ru.dstu.dormitory.reports_service.web.dto.response;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record RequestRowDto(
        UUID requestId,
        String type,
        String status,
        String authorName,
        String assigneeName,
        String roomNumber,
        Instant createdAt,
        Instant closedAt,
        Long resolutionSeconds,
        Boolean autoClosed
) {
}
