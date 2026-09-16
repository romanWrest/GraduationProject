package ru.dstu.dormitory.reports_service.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.dstu.dormitory.reports_service.domain.enums.RequestStatus;
import ru.dstu.dormitory.reports_service.domain.enums.RequestType;
import ru.dstu.dormitory.reports_service.domain.model.RequestView;
import ru.dstu.dormitory.reports_service.domain.repo.RequestViewRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestClosedHandlerTest {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock RequestViewRepository repository;
    @InjectMocks RequestClosedHandler handler;

    @Test
    void fills_closed_at_and_resolution_seconds() throws Exception {
        UUID requestId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-01-15T10:00:00Z");
        Instant closedAt = Instant.parse("2026-01-15T11:30:00Z");

        RequestView existing = RequestView.builder()
                .requestId(requestId)
                .type(RequestType.OTHER)
                .status(RequestStatus.IN_PROGRESS)
                .authorId(UUID.randomUUID())
                .createdAt(createdAt)
                .build();
        when(repository.findById(requestId)).thenReturn(Optional.of(existing));

        JsonNode payload = mapper.readTree("""
                {
                  "requestId": "%s",
                  "closedAt": "%s",
                  "autoClosed": true
                }""".formatted(requestId, closedAt));

        handler.handle(payload);

        ArgumentCaptor<RequestView> captor = ArgumentCaptor.forClass(RequestView.class);
        verify(repository).save(captor.capture());
        RequestView saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(RequestStatus.CLOSED);
        assertThat(saved.getClosedAt()).isEqualTo(closedAt);
        assertThat(saved.getResolutionSeconds()).isEqualTo(90L * 60L); // 1.5 часа
        assertThat(saved.getAutoClosed()).isTrue();
    }
}
