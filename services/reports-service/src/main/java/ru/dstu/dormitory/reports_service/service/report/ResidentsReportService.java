package ru.dstu.dormitory.reports_service.service.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.reports_service.aspect.annotation.LogMethod;
import ru.dstu.dormitory.reports_service.domain.model.ResidentView;
import ru.dstu.dormitory.reports_service.domain.repo.ResidentViewRepository;
import ru.dstu.dormitory.reports_service.util.LogPatterns;
import ru.dstu.dormitory.reports_service.util.PeriodValidator;
import ru.dstu.dormitory.reports_service.web.dto.response.ResidentRowDto;
import ru.dstu.dormitory.reports_service.web.dto.response.ResidentsReportDto;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResidentsReportService {

    private final ResidentViewRepository repository;
    private final PeriodValidator periodValidator;

    @LogMethod(value = "Построение отчёта по жильцам", logArgs = {"asOf"})
    @Transactional(readOnly = true)
    public ResidentsReportDto build(LocalDate asOf) {
        long start = System.currentTimeMillis();
        LocalDate effective = asOf == null ? LocalDate.now() : asOf;

        List<ResidentView> views = repository.findActiveAsOf(effective);
        periodValidator.checkRowLimit(views.size());

        Map<String, Long> byKind = views.stream()
                .collect(Collectors.groupingBy(v -> v.getKind().name(), LinkedHashMap::new, Collectors.counting()));
        Map<String, Long> byFaculty = views.stream()
                .filter(v -> v.getFaculty() != null && !v.getFaculty().isBlank())
                .collect(Collectors.groupingBy(ResidentView::getFaculty, LinkedHashMap::new, Collectors.counting()));
        Map<String, Long> byRoom = views.stream()
                .filter(v -> v.getRoomNumber() != null && !v.getRoomNumber().isBlank())
                .collect(Collectors.groupingBy(ResidentView::getRoomNumber, LinkedHashMap::new, Collectors.counting()));

        List<ResidentRowDto> rows = views.stream().map(v -> ResidentRowDto.builder()
                .residentId(v.getResidentId())
                .userId(v.getUserId())
                .fullName(v.getFullName())
                .kind(v.getKind().name())
                .faculty(v.getFaculty())
                .studyGroup(v.getStudyGroup())
                .department(v.getDepartment())
                .roomNumber(v.getRoomNumber())
                .enrolledAt(v.getEnrolledAt())
                .build()).toList();

        ResidentsReportDto dto = ResidentsReportDto.builder()
                .asOf(effective)
                .totalCount(rows.size())
                .byKind(byKind)
                .byFaculty(byFaculty)
                .byRoom(byRoom)
                .rows(rows)
                .build();
        log.info(LogPatterns.REPORT_BUILT, "residents", effective.toString(), rows.size(),
                System.currentTimeMillis() - start);
        return dto;
    }
}
