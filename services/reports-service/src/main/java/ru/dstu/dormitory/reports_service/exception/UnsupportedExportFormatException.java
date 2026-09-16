package ru.dstu.dormitory.reports_service.exception;

public class UnsupportedExportFormatException extends RuntimeException {

    public UnsupportedExportFormatException(String format) {
        super("Неподдерживаемый формат экспорта: %s. Доступны: csv, xlsx, pdf".formatted(format));
    }
}
