package ru.dstu.dormitory.reports_service.web.dto.response;

import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Builder
public record ConsumablesReportDto(
        Instant from,
        Instant to,
        long totalIssues,
        Map<String, Long> issuesByType,
        Map<String, Long> issuesByResident,
        long returnedCount,
        long damagedCount,
        long lostCount,
        double returnedPct,
        double damagedPct,
        double lostPct,
        List<ConsumableRowDto> rows
) {
}
