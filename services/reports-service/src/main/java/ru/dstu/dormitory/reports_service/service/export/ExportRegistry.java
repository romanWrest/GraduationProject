package ru.dstu.dormitory.reports_service.service.export;

import org.springframework.stereotype.Component;
import ru.dstu.dormitory.reports_service.exception.UnsupportedExportFormatException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Реестр экспортёров: ищет {@link ReportExporter} по {@link ExportFormat}.
 */
@Component
public class ExportRegistry {

    private final Map<ExportFormat, ReportExporter> byFormat;

    public ExportRegistry(List<ReportExporter> exporters) {
        this.byFormat = exporters.stream().collect(Collectors.toUnmodifiableMap(
                ReportExporter::format, Function.identity()));
    }

    public ReportExporter resolve(ExportFormat format) {
        ReportExporter exporter = byFormat.get(format);
        if (exporter == null) {
            throw new UnsupportedExportFormatException(format.extension());
        }
        return exporter;
    }
}
