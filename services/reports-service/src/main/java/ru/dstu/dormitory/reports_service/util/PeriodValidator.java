package ru.dstu.dormitory.reports_service.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.dstu.dormitory.reports_service.config.ReportsProperties;
import ru.dstu.dormitory.reports_service.exception.InvalidReportPeriodException;
import ru.dstu.dormitory.reports_service.exception.ReportRowLimitExceededException;

import java.time.Instant;
import java.time.Period;
import java.time.ZoneOffset;

@Component
@RequiredArgsConstructor
public class PeriodValidator {

    private final ReportsProperties properties;

    public void validate(Instant from, Instant to) {
        if (from == null || to == null) {
            throw new InvalidReportPeriodException("Параметры from и to обязательны");
        }
        if (!from.isBefore(to)) {
            throw new InvalidReportPeriodException("from должен быть раньше to");
        }
        Period period = Period.between(
                from.atOffset(ZoneOffset.UTC).toLocalDate(),
                to.atOffset(ZoneOffset.UTC).toLocalDate());
        long months = period.toTotalMonths();
        if (months > properties.getMaxPeriodMonths()) {
            throw new InvalidReportPeriodException(
                    "Период %d мес. превышает максимум %d мес.".formatted(months, properties.getMaxPeriodMonths()));
        }
    }

    public void checkRowLimit(long rowCount) {
        if (rowCount > properties.getMaxRows()) {
            throw new ReportRowLimitExceededException(rowCount, properties.getMaxRows());
        }
    }
}
