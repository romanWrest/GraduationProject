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
import ru.dstu.dormitory.reports_service.domain.model.UserView;
import ru.dstu.dormitory.reports_service.domain.repo.UserViewRepository;
import ru.dstu.dormitory.reports_service.service.LateJoinService;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCreatedHandlerTest {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock UserViewRepository repository;
    @Mock LateJoinService lateJoinService;
    @InjectMocks UserCreatedHandler handler;

    @Test
    void inserts_user_and_triggers_late_join() throws Exception {
        UUID userId = UUID.randomUUID();
        when(repository.findById(userId)).thenReturn(Optional.empty());

        JsonNode payload = mapper.readTree("""
                {
                  "userId": "%s",
                  "email": "u@example.com",
                  "fullName": "Петров П.П."
                }""".formatted(userId));

        handler.handle(payload);

        ArgumentCaptor<UserView> captor = ArgumentCaptor.forClass(UserView.class);
        verify(repository).save(captor.capture());
        UserView saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getEmail()).isEqualTo("u@example.com");
        assertThat(saved.getFullName()).isEqualTo("Петров П.П.");
        assertThat(saved.isActive()).isTrue();

        verify(lateJoinService).propagateUserName(userId, "Петров П.П.");
    }
}
