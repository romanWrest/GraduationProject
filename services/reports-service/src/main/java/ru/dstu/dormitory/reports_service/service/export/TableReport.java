package ru.dstu.dormitory.reports_service.service.export;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * Универсальная модель табличного отчёта для экспортёров.
 * <ul>
 *   <li>{@code title} — заголовок отчёта (для PDF и для metadata).</li>
 *   <li>{@code subtitle} — описание периода/фильтров.</li>
 *   <li>{@code headers} — заголовки колонок (на русском).</li>
 *   <li>{@code rows} — данные: на каждой колонке — Object (String, Number, LocalDate, Instant и т.д.).</li>
 *   <li>{@code totals} — карта label→value для секции «Итого» под таблицей.</li>
 * </ul>
 */
@Getter
@Builder
public class TableReport {

    private final String name;
    private final String title;
    private final String subtitle;
    private final List<String> headers;
    private final List<List<Object>> rows;
    private final Map<String, Object> totals;
}
