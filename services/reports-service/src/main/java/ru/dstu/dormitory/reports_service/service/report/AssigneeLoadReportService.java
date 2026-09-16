package ru.dstu.dormitory.reports_service.service.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.reports_service.aspect.annotation.LogMethod;
import ru.dstu.dormitory.reports_service.domain.enums.RequestStatus;
import ru.dstu.dormitory.reports_service.domain.model.RequestView;
import ru.dstu.dormitory.reports_service.domain.repo.RequestViewRepository;
import ru.dstu.dormitory.reports_service.util.LogPatterns;
import ru.dstu.dormitory.reports_service.util.PeriodValidator;
import ru.dstu.dormitory.reports_service.web.dto.response.AssigneeLoadReportDto;
import ru.dstu.dormitory.reports_service.web.dto.response.AssigneeLoadRowDto;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssigneeLoadReportService {

    private final RequestViewRepository repository;
    private final PeriodValidator periodValidator;

    @LogMethod(value = "Построение отчёта по загрузке исполнителей", logArgs = {"from", "to"})
    @Transactional(readOnly = true)
    public AssigneeLoadReportDto build(Instant from, Instant to) {
        periodValidator.validate(from, to);
        long start = System.currentTimeMillis();

        List<RequestView> views = repository.findForReport(from, to, null, null, null).stream()
                .filter(v -> v.getAssigneeId() != null)
                .toList();
        periodValidator.checkRowLimit(views.size());

        List<AssigneeLoadRowDto> rows = views.stream()
                .collect(Collectors.groupingBy(RequestView::getAssigneeId))
                .entrySet().stream()
                .map(e -> toRow(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingLong(AssigneeLoadRowDto::assignedCount).reversed())
                .toList();

        AssigneeLoadReportDto dto = AssigneeLoadReportDto.builder()
                .from(from).to(to)
                .totalAssignees(rows.size())
                .rows(rows)
                .build();
        log.info(LogPatterns.REPORT_BUILT, "assignees", "%s..%s".formatted(from, to),
                rows.size(), System.currentTimeMillis() - start);
        return dto;
    }

    private AssigneeLoadRowDto toRow(UUID assigneeId, List<RequestView> requests) {
        long completed = requests.stream()
                .filter(r -> r.getStatus() == RequestStatus.CLOSED || r.getStatus() == RequestStatus.DONE)
                .count();
        Long avg = requests.stream()
                .filter(r -> r.getResolutionSeconds() != null)
                .mapToLong(RequestView::getResolutionSeconds)
                .average()
                .stream().mapToLong(d -> (long) d).boxed().findFirst().orElse(null);

        String name = requests.stream()
                .map(RequestView::getAssigneeName)
                .filter(n -> n != null && !n.isBlank())
                .findFirst()
                .orElse(assigneeId.toString());

        return AssigneeLoadRowDto.builder()
                .assigneeId(assigneeId)
                .assigneeName(name)
                .assignedCount(requests.size())
                .completedCount(completed)
                .avgResolutionSeconds(avg)
                .build();
    }
}
