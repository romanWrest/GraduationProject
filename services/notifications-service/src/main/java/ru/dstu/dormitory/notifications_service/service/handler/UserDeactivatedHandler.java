package ru.dstu.dormitory.notifications_service.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.dstu.dormitory.notifications_service.aspect.annotation.LogMethod;
import ru.dstu.dormitory.notifications_service.client.AuthFacade;
import ru.dstu.dormitory.notifications_service.domain.enums.NotificationType;
import ru.dstu.dormitory.notifications_service.service.DispatchService;
import ru.dstu.dormitory.notifications_service.service.TemplateService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserDeactivatedHandler implements EventHandler {

    private static final String TEMPLATE = "account-deactivated";
    private static final String SUBJECT = "Ваш аккаунт деактивирован";

    private final TemplateService templateService;
    private final DispatchService dispatchService;
    private final AuthFacade authFacade;

    @Override
    public String eventType() {
        return "UserDeactivated";
    }

    @Override
    @LogMethod(value = "Обработка UserDeactivated")
    public void handle(JsonNode payload) {
        UUID userId = HandlerSupport.extractUuid(payload, "userId");
        if (userId == null) {
            return;
        }
        String userName = HandlerSupport.userName(authFacade.getUser(userId));

        Map<String, Object> vars = new HashMap<>();
        vars.put("userName", userName);

        String html = templateService.render(TEMPLATE, vars);

        dispatchService.dispatch(
                userId,
                NotificationType.ACCOUNT_DEACTIVATED,
                SUBJECT,
                "Ваш аккаунт был деактивирован администратором.",
                Map.of("userId", userId.toString()),
                TEMPLATE,
                SUBJECT,
                html);
    }
}
