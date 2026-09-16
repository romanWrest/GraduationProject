package ru.dstu.dormitory.reports_service.service.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.reports_service.aspect.annotation.LogMethod;
import ru.dstu.dormitory.reports_service.domain.enums.ConsumableStatus;
import ru.dstu.dormitory.reports_service.domain.enums.ReturnCondition;
import ru.dstu.dormitory.reports_service.domain.model.ConsumableView;
import ru.dstu.dormitory.reports_service.domain.repo.ConsumableViewRepository;
import ru.dstu.dormitory.reports_service.util.LogPatterns;
import ru.dstu.dormitory.reports_service.util.PeriodValidator;
import ru.dstu.dormitory.reports_service.web.dto.response.ConsumableRowDto;
import ru.dstu.dormitory.reports_service.web.dto.response.ConsumablesReportDto;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumablesReportService {

    private final ConsumableViewRepository repository;
    private final PeriodValidator periodValidator;

    @LogMethod(value = "Построение отчёта по расходникам", logArgs = {"from", "to", "typeId"})
    @Transactional(readOnly = true)
    public ConsumablesReportDto build(Instant from, Instant to, UUID typeId) {
        periodValidator.validate(from, to);
        long start = System.currentTimeMillis();

        List<ConsumableView> views = repository.findForReport(from, to, typeId);
        periodValidator.checkRowLimit(views.size());

        long total = views.size();
        long returned = views.stream().filter(v -> v.getStatus() == ConsumableStatus.RETURNED
                && v.getReturnCondition() == ReturnCondition.OK).count();
        long damaged = views.stream().filter(v -> v.getReturnCondition() == ReturnCondition.DAMAGED).count();
        long lost = views.stream().filter(v -> v.getReturnCondition() == ReturnCondition.LOST
                || v.getStatus() == ConsumableStatus.LOST).count();

        double pct = total == 0 ? 0.0 : 100.0;

        Map<String, Long> byType = views.stream()
                .collect(Collectors.groupingBy(
                        v -> v.getTypeName() == null ? v.getTypeId().toString() : v.getTypeName(),
                        LinkedHashMap::new,
                        Collectors.counting()));
        Map<String, Long> byResident = views.stream()
                .collect(Collectors.groupingBy(
                        v -> v.getResidentName() == null ? v.getUserId().toString() : v.getResidentName(),
                        LinkedHashMap::new,
                        Collectors.counting()));

        List<ConsumableRowDto> rows = views.stream().map(v -> ConsumableRowDto.builder()
                .issueId(v.getIssueId())
                .residentName(v.getResidentName())
                .typeName(v.getTypeName())
                .quantity(v.getQuantity())
                .status(v.getStatus().name())
                .issuedAt(v.getIssuedAt())
                .returnedAt(v.getReturnedAt())
                .returnCondition(v.getReturnCondition() == null ? null : v.getReturnCondition().name())
                .build()).toList();

        ConsumablesReportDto dto = ConsumablesReportDto.builder()
                .from(from).to(to)
                .totalIssues(total)
                .issuesByType(byType)
                .issuesByResident(byResident)
                .returnedCount(returned)
                .damagedCount(damaged)
                .lostCount(lost)
                .returnedPct(total == 0 ? 0 : returned * pct / total)
                .damagedPct(total == 0 ? 0 : damaged * pct / total)
                .lostPct(total == 0 ? 0 : lost * pct / total)
                .rows(rows)
                .build();

        log.info(LogPatterns.REPORT_BUILT, "consumables", "%s..%s".formatted(from, to),
                rows.size(), System.currentTimeMillis() - start);
        return dto;
    }
}
