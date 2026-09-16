package ru.dstu.dormitory.reports_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.reports")
public class ReportsProperties {

    /**
     * Максимально допустимое число строк в ответе/экспорте.
     */
    private long maxRows = 100_000L;

    /**
     * Максимальная длительность периода (в месяцах).
     */
    private int maxPeriodMonths = 24;
}
