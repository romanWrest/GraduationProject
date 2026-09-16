package ru.dstu.dormitory.reports_service.web.dto.response;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Builder
public record ResidentsReportDto(
        LocalDate asOf,
        long totalCount,
        Map<String, Long> byKind,
        Map<String, Long> byFaculty,
        Map<String, Long> byRoom,
        List<ResidentRowDto> rows
) {
}
