package ru.dstu.dormitory.notifications_service.service.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.dstu.dormitory.notifications_service.client.AuthFacade;
import ru.dstu.dormitory.notifications_service.client.dto.AuthUserDto;
import ru.dstu.dormitory.notifications_service.config.AppFrontendProperties;
import ru.dstu.dormitory.notifications_service.domain.enums.NotificationType;
import ru.dstu.dormitory.notifications_service.service.DispatchService;
import ru.dstu.dormitory.notifications_service.service.TemplateService;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCreatedHandlerTest {

    @Mock TemplateService templateService;
    @Mock DispatchService dispatchService;
    @Mock AuthFacade authFacade;

    AppFrontendProperties frontend = new AppFrontendProperties();

    UserCreatedHandler handler;

    @Test
    void rendersWelcomeAndDispatches() throws Exception {
        handler = new UserCreatedHandler(templateService, dispatchService, authFacade, frontend);

        UUID userId = UUID.randomUUID();
        when(authFacade.getUser(userId)).thenReturn(new AuthUserDto(userId,
                "ivan@example.com", "Иван Иванов", null, true, Set.of(),
                Instant.now(), Instant.now()));
        when(templateService.render(eq("welcome"), anyMap())).thenReturn("<html/>");

        ObjectMapper om = new ObjectMapper();
        String json = """
                { "userId": "%s", "email": "ivan@example.com",
                  "fullName": "Иван Иванов", "temporaryPassword": "p1" }
                """.formatted(userId);

        handler.handle(om.readTree(json));

        ArgumentCaptor<String> templateCaptor = ArgumentCaptor.forClass(String.class);
        verify(dispatchService).dispatch(eq(userId), eq(NotificationType.WELCOME),
                anyString(), anyString(), any(), templateCaptor.capture(), anyString(), eq("<html/>"));
        assertThat(templateCaptor.getValue()).isEqualTo("welcome");
    }
}
