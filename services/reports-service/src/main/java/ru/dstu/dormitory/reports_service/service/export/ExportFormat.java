package ru.dstu.dormitory.reports_service.service.export;

import org.springframework.http.MediaType;
import ru.dstu.dormitory.reports_service.exception.UnsupportedExportFormatException;

public enum ExportFormat {
    CSV("csv", "text/csv;charset=UTF-8"),
    XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    PDF("pdf", MediaType.APPLICATION_PDF_VALUE);

    private final String extension;
    private final String contentType;

    ExportFormat(String extension, String contentType) {
        this.extension = extension;
        this.contentType = contentType;
    }

    public String extension() {
        return extension;
    }

    public String contentType() {
        return contentType;
    }

    public static ExportFormat from(String value) {
        if (value == null) {
            throw new UnsupportedExportFormatException("null");
        }
        for (ExportFormat f : values()) {
            if (f.extension.equalsIgnoreCase(value)) {
                return f;
            }
        }
        throw new UnsupportedExportFormatException(value);
    }
}
