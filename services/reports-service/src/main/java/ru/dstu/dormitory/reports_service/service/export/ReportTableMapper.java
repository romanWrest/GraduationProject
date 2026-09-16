package ru.dstu.dormitory.reports_service.service.export;

import org.springframework.stereotype.Component;
import ru.dstu.dormitory.reports_service.web.dto.response.AppliancesReportDto;
import ru.dstu.dormitory.reports_service.web.dto.response.AssigneeLoadReportDto;
import ru.dstu.dormitory.reports_service.web.dto.response.ConsumablesReportDto;
import ru.dstu.dormitory.reports_service.web.dto.response.RequestsReportDto;
import ru.dstu.dormitory.reports_service.web.dto.response.ResidentsReportDto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Мапит DTO отчётов в плоскую {@link TableReport} для экспортёров.
 */
@Component
public class ReportTableMapper {

    public TableReport mapRequests(RequestsReportDto dto) {
        List<List<Object>> rows = dto.rows().stream()
                .<List<Object>>map(r -> List.of(
                        safe(r.requestId()),
                        safe(r.type()),
                        safe(r.status()),
                        safe(r.authorName()),
                        safe(r.assigneeName()),
                        safe(r.roomNumber()),
                        safe(r.createdAt()),
                        safe(r.closedAt()),
                        safe(r.resolutionSeconds()),
                        safe(r.autoClosed())))
                .toList();
        Map<String, Object> totals = new LinkedHashMap<>();
        totals.put("Всего заявок", dto.totalCount());
        if (dto.avgResolutionSeconds() != null) {
            totals.put("Среднее время закрытия (сек)", dto.avgResolutionSeconds());
        }
        return TableReport.builder()
                .name("requests")
                .title("Отчёт по заявкам")
                .subtitle("Период: %s — %s".formatted(dto.from(), dto.to()))
                .headers(List.of("ID", "Тип", "Статус", "Автор", "Исполнитель",
                        "Комната", "Создана", "Закрыта", "Срок (сек)", "Авто-закрытие"))
                .rows(rows)
                .totals(totals)
                .build();
    }

    public TableReport mapAssignees(AssigneeLoadReportDto dto) {
        List<List<Object>> rows = dto.rows().stream()
                .<List<Object>>map(r -> List.of(
                        safe(r.assigneeName()),
                        r.assignedCount(),
                        r.completedCount(),
                        safe(r.avgResolutionSeconds())))
                .toList();
        return TableReport.builder()
                .name("assignees")
                .title("Загрузка исполнителей")
                .subtitle("Период: %s — %s".formatted(dto.from(), dto.to()))
                .headers(List.of("Исполнитель", "Назначено", "Закрыто", "Среднее время (сек)"))
                .rows(rows)
                .totals(Map.of("Всего исполнителей", dto.totalAssignees()))
                .build();
    }

    public TableReport mapResidents(ResidentsReportDto dto) {
        List<List<Object>> rows = dto.rows().stream()
                .<List<Object>>map(r -> List.of(
                        safe(r.fullName()),
                        safe(r.kind()),
                        safe(r.faculty()),
                        safe(r.studyGroup()),
                        safe(r.department()),
                        safe(r.roomNumber()),
                        safe(r.enrolledAt())))
                .toList();
        return TableReport.builder()
                .name("residents")
                .title("Отчёт по жильцам")
                .subtitle("На дату: %s".formatted(dto.asOf()))
                .headers(List.of("ФИО", "Тип", "Факультет", "Группа", "Подразделение",
                        "Комната", "Дата заселения"))
                .rows(rows)
                .totals(Map.of("Всего жильцов", dto.totalCount()))
                .build();
    }

    public TableReport mapAppliances(AppliancesReportDto dto) {
        List<List<Object>> rows = dto.rows().stream()
                .<List<Object>>map(r -> List.of(
                        safe(r.residentName()),
                        safe(r.roomNumber()),
                        safe(r.type()),
                        safe(r.brand()),
                        safe(r.model()),
                        safe(r.powerWatts()),
                        safe(r.status())))
                .toList();
        Map<String, Object> totals = new LinkedHashMap<>();
        totals.put("Всего приборов", dto.totalCount());
        totals.put("Суммарная мощность (Вт)", dto.totalPowerWatts());
        return TableReport.builder()
                .name("appliances")
                .title("Отчёт по электроприборам")
                .subtitle("Все статусы / комнаты")
                .headers(List.of("Жилец", "Комната", "Тип", "Бренд", "Модель",
                        "Мощность (Вт)", "Статус"))
                .rows(rows)
                .totals(totals)
                .build();
    }

    public TableReport mapConsumables(ConsumablesReportDto dto) {
        List<List<Object>> rows = dto.rows().stream()
                .<List<Object>>map(r -> List.of(
                        safe(r.residentName()),
                        safe(r.typeName()),
                        safe(r.quantity()),
                        safe(r.status()),
                        safe(r.issuedAt()),
                        safe(r.returnedAt()),
                        safe(r.returnCondition())))
                .toList();
        Map<String, Object> totals = new LinkedHashMap<>();
        totals.put("Всего выдач", dto.totalIssues());
        totals.put("Возвращено OK (%)", "%.1f".formatted(dto.returnedPct()));
        totals.put("Повреждено (%)", "%.1f".formatted(dto.damagedPct()));
        totals.put("Утрачено (%)", "%.1f".formatted(dto.lostPct()));
        return TableReport.builder()
                .name("consumables")
                .title("Отчёт по выдачам расходников")
                .subtitle("Период: %s — %s".formatted(dto.from(), dto.to()))
                .headers(List.of("Жилец", "Тип", "Кол-во", "Статус",
                        "Выдано", "Возвращено", "Состояние"))
                .rows(rows)
                .totals(totals)
                .build();
    }

    private Object safe(Object value) {
        return value == null ? "" : value;
    }
}
