package ru.dstu.dormitory.reports_service.web.dto.response;

import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record AssigneeLoadReportDto(
        Instant from,
        Instant to,
        long totalAssignees,
        List<AssigneeLoadRowDto> rows
) {
}
