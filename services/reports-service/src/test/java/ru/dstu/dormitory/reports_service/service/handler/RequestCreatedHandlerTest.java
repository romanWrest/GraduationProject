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
import ru.dstu.dormitory.reports_service.domain.enums.TargetPool;
import ru.dstu.dormitory.reports_service.domain.model.RequestView;
import ru.dstu.dormitory.reports_service.domain.model.UserView;
import ru.dstu.dormitory.reports_service.domain.repo.RequestViewRepository;
import ru.dstu.dormitory.reports_service.domain.repo.UserViewRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestCreatedHandlerTest {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock RequestViewRepository repository;
    @Mock UserViewRepository userViewRepository;

    @InjectMocks RequestCreatedHandler handler;

    @Test
    void inserts_request_with_author_name_when_user_view_present() throws Exception {
        UUID requestId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-01-15T10:00:00Z");

        when(userViewRepository.findById(authorId)).thenReturn(Optional.of(
                UserView.builder().userId(authorId).fullName("Иванов И.И.").build()));
        when(repository.findById(requestId)).thenReturn(Optional.empty());

        JsonNode payload = mapper.readTree("""
                {
                  "requestId": "%s",
                  "type": "REPAIR_PLUMBING",
                  "authorId": "%s",
                  "roomId": "%s",
                  "targetPool": "EXECUTOR_PLUMBING",
                  "createdAt": "%s"
                }""".formatted(requestId, authorId, roomId, createdAt));

        handler.handle(payload);

        ArgumentCaptor<RequestView> captor = ArgumentCaptor.forClass(RequestView.class);
        verify(repository).save(captor.capture());
        RequestView saved = captor.getValue();
        assertThat(saved.getRequestId()).isEqualTo(requestId);
        assertThat(saved.getType()).isEqualTo(RequestType.REPAIR_PLUMBING);
        assertThat(saved.getStatus()).isEqualTo(RequestStatus.NEW);
        assertThat(saved.getAuthorName()).isEqualTo("Иванов И.И.");
        assertThat(saved.getTargetPool()).isEqualTo(TargetPool.EXECUTOR_PLUMBING);
        assertThat(saved.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void inserts_with_null_author_name_when_user_view_missing() throws Exception {
        UUID requestId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-01-15T10:00:00Z");

        when(userViewRepository.findById(authorId)).thenReturn(Optional.empty());
        when(repository.findById(any())).thenReturn(Optional.empty());

        JsonNode payload = mapper.readTree("""
                {
                  "requestId": "%s",
                  "type": "OTHER",
                  "authorId": "%s",
                  "createdAt": "%s"
                }""".formatted(requestId, authorId, createdAt));

        handler.handle(payload);

        ArgumentCaptor<RequestView> captor = ArgumentCaptor.forClass(RequestView.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getAuthorName()).isNull();
    }
}
