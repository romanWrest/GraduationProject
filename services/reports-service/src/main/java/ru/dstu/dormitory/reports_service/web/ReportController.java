package ru.dstu.dormitory.reports_service.web;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.dstu.dormitory.reports_service.domain.enums.ApplianceStatus;
import ru.dstu.dormitory.reports_service.domain.enums.RequestStatus;
import ru.dstu.dormitory.reports_service.domain.enums.RequestType;
import ru.dstu.dormitory.reports_service.service.report.AppliancesReportService;
import ru.dstu.dormitory.reports_service.service.report.AssigneeLoadReportService;
import ru.dstu.dormitory.reports_service.service.report.ConsumablesReportService;
import ru.dstu.dormitory.reports_service.service.report.RequestsReportService;
import ru.dstu.dormitory.reports_service.service.report.ResidentsReportService;
import ru.dstu.dormitory.reports_service.web.dto.response.AppliancesReportDto;
import ru.dstu.dormitory.reports_service.web.dto.response.AssigneeLoadReportDto;
import ru.dstu.dormitory.reports_service.web.dto.response.ConsumablesReportDto;
import ru.dstu.dormitory.reports_service.web.dto.response.RequestsReportDto;
import ru.dstu.dormitory.reports_service.web.dto.response.ResidentsReportDto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final RequestsReportService requestsReportService;
    private final AssigneeLoadReportService assigneeLoadReportService;
    private final ResidentsReportService residentsReportService;
    private final AppliancesReportService appliancesReportService;
    private final ConsumablesReportService consumablesReportService;

    private static final int DEFAULT_PERIOD_DAYS = 30;

    @GetMapping("/requests")
    public RequestsReportDto getRequestsReport(@RequestParam(required = false) Instant from,
                                               @RequestParam(required = false) Instant to,
                                               @RequestParam(required = false) RequestType type,
                                               @RequestParam(required = false) RequestStatus status,
                                               @RequestParam(required = false) UUID assigneeId) {
        Instant resolvedTo = to != null ? to : Instant.now();
        Instant resolvedFrom = from != null ? from : resolvedTo.minus(DEFAULT_PERIOD_DAYS, ChronoUnit.DAYS);
        return requestsReportService.build(resolvedFrom, resolvedTo, type, status, assigneeId);
    }

    @GetMapping("/assignees")
    public AssigneeLoadReportDto getAssigneeLoad(@RequestParam(required = false) Instant from,
                                                 @RequestParam(required = false) Instant to) {
        Instant resolvedTo = to != null ? to : Instant.now();
        Instant resolvedFrom = from != null ? from : resolvedTo.minus(DEFAULT_PERIOD_DAYS, ChronoUnit.DAYS);
        return assigneeLoadReportService.build(resolvedFrom, resolvedTo);
    }

    @GetMapping("/residents")
    public ResidentsReportDto getResidents(@RequestParam(required = false)
                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOf) {
        return residentsReportService.build(asOf);
    }

    @GetMapping("/appliances")
    public AppliancesReportDto getAppliances(@RequestParam(required = false) UUID roomId,
                                             @RequestParam(required = false) ApplianceStatus status) {
        return appliancesReportService.build(roomId, status);
    }

    @GetMapping("/consumables")
    public ConsumablesReportDto getConsumables(@RequestParam(required = false) Instant from,
                                               @RequestParam(required = false) Instant to,
                                               @RequestParam(required = false) UUID typeId) {
        Instant resolvedTo = to != null ? to : Instant.now();
        Instant resolvedFrom = from != null ? from : resolvedTo.minus(DEFAULT_PERIOD_DAYS, ChronoUnit.DAYS);
        return consumablesReportService.build(resolvedFrom, resolvedTo, typeId);
    }
}
