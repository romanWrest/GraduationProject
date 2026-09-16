package ru.dstu.dormitory.reports_service.web.dto.response;

import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Builder
public record RequestsReportDto(
        Instant from,
        Instant to,
        long totalCount,
        Map<String, Long> byStatus,
        Map<String, Long> byType,
        Map<String, Long> byAuthor,
        Map<String, Long> byAssignee,
        Long avgResolutionSeconds,
        List<RequestRowDto> rows
) {
}
