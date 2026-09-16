package ru.dstu.dormitory.reports_service.exception;

public class ReportRowLimitExceededException extends RuntimeException {

    public ReportRowLimitExceededException(long rowCount, long limit) {
        super("Размер выгрузки %d превышает лимит %d. Сузьте фильтры (период, тип, статус)."
                .formatted(rowCount, limit));
    }
}
