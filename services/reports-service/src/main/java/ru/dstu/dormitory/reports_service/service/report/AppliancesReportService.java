package ru.dstu.dormitory.reports_service.service.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.reports_service.aspect.annotation.LogMethod;
import ru.dstu.dormitory.reports_service.domain.enums.ApplianceStatus;
import ru.dstu.dormitory.reports_service.domain.model.ApplianceView;
import ru.dstu.dormitory.reports_service.domain.repo.ApplianceViewRepository;
import ru.dstu.dormitory.reports_service.util.LogPatterns;
import ru.dstu.dormitory.reports_service.util.PeriodValidator;
import ru.dstu.dormitory.reports_service.web.dto.response.ApplianceRowDto;
import ru.dstu.dormitory.reports_service.web.dto.response.AppliancesReportDto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppliancesReportService {

    private final ApplianceViewRepository repository;
    private final PeriodValidator periodValidator;

    @LogMethod(value = "Построение отчёта по электроприборам", logArgs = {"roomId", "status"})
    @Transactional(readOnly = true)
    public AppliancesReportDto build(UUID roomId, ApplianceStatus status) {
        long start = System.currentTimeMillis();

        List<ApplianceView> views = repository.findForReport(roomId, status);
        periodValidator.checkRowLimit(views.size());

        long totalPower = views.stream().mapToLong(ApplianceView::getPowerWatts).sum();
        Map<String, Long> totalPowerByRoom = views.stream()
                .filter(v -> v.getRoomNumber() != null && !v.getRoomNumber().isBlank())
                .collect(Collectors.groupingBy(
                        ApplianceView::getRoomNumber,
                        LinkedHashMap::new,
                        Collectors.summingLong(ApplianceView::getPowerWatts)));
        Map<String, Long> countByStatus = views.stream()
                .collect(Collectors.groupingBy(v -> v.getStatus().name(), LinkedHashMap::new, Collectors.counting()));

        List<ApplianceRowDto> rows = views.stream().map(v -> ApplianceRowDto.builder()
                .applianceId(v.getApplianceId())
                .residentName(v.getResidentName())
                .roomNumber(v.getRoomNumber())
                .type(v.getType())
                .brand(v.getBrand())
                .model(v.getModel())
                .powerWatts(v.getPowerWatts())
                .status(v.getStatus().name())
                .build()).toList();

        AppliancesReportDto dto = AppliancesReportDto.builder()
                .totalCount(rows.size())
                .totalPowerWatts(totalPower)
                .totalPowerByRoom(totalPowerByRoom)
                .countByStatus(countByStatus)
                .rows(rows)
                .build();
        log.info(LogPatterns.REPORT_BUILT, "appliances", "all", rows.size(),
                System.currentTimeMillis() - start);
        return dto;
    }
}
