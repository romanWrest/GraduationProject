package ru.dstu.dormitory.reports_service.web.dto.response;

import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record AppliancesReportDto(
        long totalCount,
        long totalPowerWatts,
        Map<String, Long> totalPowerByRoom,
        Map<String, Long> countByStatus,
        List<ApplianceRowDto> rows
) {
}
