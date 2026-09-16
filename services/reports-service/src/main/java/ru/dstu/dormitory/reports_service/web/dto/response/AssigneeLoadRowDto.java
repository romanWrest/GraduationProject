package ru.dstu.dormitory.reports_service.web.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record AssigneeLoadRowDto(
        UUID assigneeId,
        String assigneeName,
        long assignedCount,
        long completedCount,
        Long avgResolutionSeconds
) {
}
