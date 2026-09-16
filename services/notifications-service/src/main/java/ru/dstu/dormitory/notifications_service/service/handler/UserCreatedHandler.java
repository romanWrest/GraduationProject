package ru.dstu.dormitory.notifications_service.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.dstu.dormitory.notifications_service.aspect.annotation.LogMethod;
import ru.dstu.dormitory.notifications_service.client.AuthFacade;
import ru.dstu.dormitory.notifications_service.client.dto.AuthUserDto;
import ru.dstu.dormitory.notifications_service.config.AppFrontendProperties;
import ru.dstu.dormitory.notifications_service.domain.enums.NotificationType;
import ru.dstu.dormitory.notifications_service.service.DispatchService;
import ru.dstu.dormitory.notifications_service.service.TemplateService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserCreatedHandler implements EventHandler {

    private static final String TEMPLATE = "welcome";
    private static final String SUBJECT = "Добро пожаловать в систему общежития";

    private final TemplateService templateService;
    private final DispatchService dispatchService;
    private final AuthFacade authFacade;
    private final AppFrontendProperties frontend;

    @Override
    public String eventType() {
        return "UserCreated";
    }

    @Override
    @LogMethod(value = "Обработка UserCreated", maskArgs = {"payload"})
    public void handle(JsonNode payload) {
        UUID userId = HandlerSupport.extractUuid(payload, "userId");
        if (userId == null) {
            return;
        }
        String email = HandlerSupport.extractText(payload, "email");
        String fullName = HandlerSupport.extractText(payload, "fullName");
        String tempPassword = HandlerSupport.extractText(payload, "temporaryPassword");

        AuthUserDto user = authFacade.getUser(userId);
        String userName = user != null ? HandlerSupport.userName(user)
                : (fullName != null ? fullName : email);

        Map<String, Object> vars = new HashMap<>();
        vars.put("userName", userName);
        vars.put("email", email);
        vars.put("tempPassword", tempPassword == null ? "—" : tempPassword);
        vars.put("actionUrl", frontend.getBaseUrl() + "/login");

        String html = templateService.render(TEMPLATE, vars);

        Map<String, Object> appPayload = new HashMap<>();
        appPayload.put("userId", userId.toString());

        dispatchService.dispatch(
                userId,
                NotificationType.WELCOME,
                SUBJECT,
                "Аккаунт создан. Временный пароль отправлен на email.",
                appPayload,
                TEMPLATE,
                SUBJECT,
                html);
    }
}
