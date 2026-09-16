package ru.dstu.dormitory.reports_service.service.export;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.dstu.dormitory.reports_service.exception.ReportNotFoundException;
import ru.dstu.dormitory.reports_service.service.report.AppliancesReportService;
import ru.dstu.dormitory.reports_service.service.report.AssigneeLoadReportService;
import ru.dstu.dormitory.reports_service.service.report.ConsumablesReportService;
import ru.dstu.dormitory.reports_service.service.report.RequestsReportService;
import ru.dstu.dormitory.reports_service.service.report.ResidentsReportService;
import ru.dstu.dormitory.reports_service.util.LogPatterns;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Универсальный экспорт отчёта: вычисляет нужный отчёт, мапит в TableReport,
 * передаёт в выбранный {@link ReportExporter}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExportService {

    private final RequestsReportService requestsReportService;
    private final AssigneeLoadReportService assigneeLoadReportService;
    private final ResidentsReportService residentsReportService;
    private final AppliancesReportService appliancesReportService;
    private final ConsumablesReportService consumablesReportService;

    private final ReportTableMapper tableMapper;
    private final ExportRegistry exportRegistry;

    public ExportResult export(String name, ExportFormat format, ExportParams params) {
        long start = System.currentTimeMillis();

        TableReport table = switch (name == null ? "" : name.toLowerCase()) {
            case "requests" -> tableMapper.mapRequests(
                    requestsReportService.build(params.from(), params.to(), null, null, params.assigneeId()));
            case "assignees" -> tableMapper.mapAssignees(
                    assigneeLoadReportService.build(params.from(), params.to()));
            case "residents" -> tableMapper.mapResidents(
                    residentsReportService.build(params.asOf()));
            case "appliances" -> tableMapper.mapAppliances(
                    appliancesReportService.build(params.roomId(), null));
            case "consumables" -> tableMapper.mapConsumables(
                    consumablesReportService.build(params.from(), params.to(), params.typeId()));
            default -> throw new ReportNotFoundException("Неизвестный отчёт: " + name);
        };

        ReportExporter exporter = exportRegistry.resolve(format);
        byte[] bytes = exporter.export(table);

        log.info(LogPatterns.REPORT_EXPORTED, name, format.extension(),
                bytes.length, System.currentTimeMillis() - start);
        return new ExportResult(bytes, format, table.getName());
    }

    public record ExportResult(byte[] bytes, ExportFormat format, String name) {
    }

    public record ExportParams(Instant from, Instant to, LocalDate asOf, UUID typeId,
                               UUID assigneeId, UUID roomId) {
    }
}
