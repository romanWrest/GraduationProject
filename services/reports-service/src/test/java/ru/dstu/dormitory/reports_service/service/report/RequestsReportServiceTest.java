package ru.dstu.dormitory.reports_service.service.report;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.dstu.dormitory.reports_service.config.ReportsProperties;
import ru.dstu.dormitory.reports_service.domain.enums.RequestStatus;
import ru.dstu.dormitory.reports_service.domain.enums.RequestType;
import ru.dstu.dormitory.reports_service.domain.model.RequestView;
import ru.dstu.dormitory.reports_service.domain.repo.RequestViewRepository;
import ru.dstu.dormitory.reports_service.exception.InvalidReportPeriodException;
import ru.dstu.dormitory.reports_service.util.PeriodValidator;
import ru.dstu.dormitory.reports_service.web.dto.response.RequestsReportDto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestsReportServiceTest {

    @Mock RequestViewRepository repository;

    private RequestsReportService service;

    @BeforeEach
    void setUp() {
        ReportsProperties props = new ReportsProperties();
        PeriodValidator validator = new PeriodValidator(props);
        service = new RequestsReportService(repository, validator);
    }

    @Test
    void aggregates_by_status_and_type_and_avg_resolution() {
        Instant from = Instant.parse("2026-01-01T00:00:00Z");
        Instant to = Instant.parse("2026-02-01T00:00:00Z");
        UUID author = UUID.randomUUID();

        RequestView a = view(RequestType.REPAIR_PLUMBING, RequestStatus.CLOSED, author, 1200L);
        RequestView b = view(RequestType.REPAIR_PLUMBING, RequestStatus.NEW, author, null);
        RequestView c = view(RequestType.OTHER, RequestStatus.CLOSED, author, 600L);

        when(repository.findForReport(any(), any(), any(), any(), any()))
                .thenReturn(List.of(a, b, c));

        RequestsReportDto dto = service.build(from, to, null, null, null);

        assertThat(dto.totalCount()).isEqualTo(3);
        assertThat(dto.byStatus()).containsEntry("CLOSED", 2L).containsEntry("NEW", 1L);
        assertThat(dto.byType()).containsEntry("REPAIR_PLUMBING", 2L).containsEntry("OTHER", 1L);
        assertThat(dto.avgResolutionSeconds()).isEqualTo(900L); // (1200+600)/2
        assertThat(dto.rows()).hasSize(3);
    }

    @Test
    void rejects_inverted_period() {
        Instant from = Instant.parse("2026-02-01T00:00:00Z");
        Instant to = Instant.parse("2026-01-01T00:00:00Z");
        assertThatThrownBy(() -> service.build(from, to, null, null, null))
                .isInstanceOf(InvalidReportPeriodException.class);
    }

    private RequestView view(RequestType type, RequestStatus status, UUID author, Long resolution) {
        return RequestView.builder()
                .requestId(UUID.randomUUID())
                .type(type)
                .status(status)
                .authorId(author)
                .authorName("Иванов И.")
                .createdAt(Instant.parse("2026-01-15T10:00:00Z"))
                .resolutionSeconds(resolution)
                .build();
    }
}
