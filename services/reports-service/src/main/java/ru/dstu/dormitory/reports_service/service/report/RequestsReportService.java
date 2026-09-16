package ru.dstu.dormitory.reports_service.service.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.reports_service.aspect.annotation.LogMethod;
import ru.dstu.dormitory.reports_service.domain.enums.RequestStatus;
import ru.dstu.dormitory.reports_service.domain.enums.RequestType;
import ru.dstu.dormitory.reports_service.domain.model.RequestView;
import ru.dstu.dormitory.reports_service.domain.repo.RequestViewRepository;
import ru.dstu.dormitory.reports_service.util.LogPatterns;
import ru.dstu.dormitory.reports_service.util.PeriodValidator;
import ru.dstu.dormitory.reports_service.web.dto.response.RequestRowDto;
import ru.dstu.dormitory.reports_service.web.dto.response.RequestsReportDto;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestsReportService {

    private final RequestViewRepository repository;
    private final PeriodValidator periodValidator;

    @LogMethod(value = "Построение отчёта по заявкам", logArgs = {"from", "to", "type", "status", "assigneeId"})
    @Transactional(readOnly = true)
    public RequestsReportDto build(Instant from, Instant to,
                                   RequestType type, RequestStatus status, UUID assigneeId) {
        periodValidator.validate(from, to);
        long start = System.currentTimeMillis();
        List<RequestView> views = repository.findForReport(from, to, type, status, assigneeId);
        periodValidator.checkRowLimit(views.size());

        Map<String, Long> byStatus = group(views, v -> v.getStatus().name());
        Map<String, Long> byType = group(views, v -> v.getType().name());
        Map<String, Long> byAuthor = group(views, v -> nullSafe(v.getAuthorName(), v.getAuthorId()));
        Map<String, Long> byAssignee = views.stream()
                .filter(v -> v.getAssigneeId() != null)
                .collect(Collectors.groupingBy(
                        v -> nullSafe(v.getAssigneeName(), v.getAssigneeId()),
                        LinkedHashMap::new,
                        Collectors.counting()));

        Long avgResolutionSeconds = views.stream()
                .filter(v -> v.getResolutionSeconds() != null)
                .mapToLong(RequestView::getResolutionSeconds)
                .average()
                .stream().mapToLong(d -> (long) d).boxed().findFirst().orElse(null);

        List<RequestRowDto> rows = views.stream()
                .map(this::toRow)
                .toList();

        RequestsReportDto dto = RequestsReportDto.builder()
                .from(from).to(to)
                .totalCount(views.size())
                .byStatus(byStatus)
                .byType(byType)
                .byAuthor(byAuthor)
                .byAssignee(byAssignee)
                .avgResolutionSeconds(avgResolutionSeconds)
                .rows(rows)
                .build();

        log.info(LogPatterns.REPORT_BUILT, "requests", "%s..%s".formatted(from, to),
                views.size(), System.currentTimeMillis() - start);
        return dto;
    }

    private Map<String, Long> group(List<RequestView> views, java.util.function.Function<RequestView, String> key) {
        return views.stream().collect(Collectors.groupingBy(key, LinkedHashMap::new, Collectors.counting()));
    }

    private String nullSafe(String name, UUID id) {
        if (name != null && !name.isBlank()) {
            return name;
        }
        return id == null ? "—" : id.toString();
    }

    private RequestRowDto toRow(RequestView v) {
        return RequestRowDto.builder()
                .requestId(v.getRequestId())
                .type(v.getType().name())
                .status(v.getStatus().name())
                .authorName(v.getAuthorName())
                .assigneeName(v.getAssigneeName())
                .roomNumber(v.getRoomNumber())
                .createdAt(v.getCreatedAt())
                .closedAt(v.getClosedAt())
                .resolutionSeconds(v.getResolutionSeconds())
                .autoClosed(v.getAutoClosed())
                .build();
    }
}
