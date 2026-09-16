package ru.dstu.dormitory.reports_service.service.export;

public interface ReportExporter {

    ExportFormat format();

    byte[] export(TableReport report);
}
