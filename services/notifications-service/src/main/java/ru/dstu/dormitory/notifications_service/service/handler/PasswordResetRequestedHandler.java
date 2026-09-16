package ru.dstu.dormitory.notifications_service.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.dstu.dormitory.notifications_service.aspect.annotation.LogMethod;
import ru.dstu.dormitory.notifications_service.client.AuthFacade;
import ru.dstu.dormitory.notifications_service.config.AppFrontendProperties;
import ru.dstu.dormitory.notifications_service.domain.enums.NotificationType;
import ru.dstu.dormitory.notifications_service.service.DispatchService;
import ru.dstu.dormitory.notifications_service.service.TemplateService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PasswordResetRequestedHandler implements EventHandler {

    private static final String TEMPLATE = "password-reset";
    private static final String SUBJECT = "Восстановление пароля";

    private final TemplateService templateService;
    private final DispatchService dispatchService;
    private final AuthFacade authFacade;
    private final AppFrontendProperties frontend;

    @Override
    public String eventType() {
        return "PasswordResetRequested";
    }

    @Override
    @LogMethod(value = "Обработка PasswordResetRequested", maskArgs = {"payload"})
    public void handle(JsonNode payload) {
        UUID userId = HandlerSupport.extractUuid(payload, "userId");
        if (userId == null) {
            return;
        }
        String resetToken = HandlerSupport.extractText(payload, "resetToken");
        String email = HandlerSupport.extractText(payload, "email");

        String userName = HandlerSupport.userName(authFacade.getUser(userId));

        String resetLink = frontend.getBaseUrl() + "/reset-password?token="
                + URLEncoder.encode(resetToken == null ? "" : resetToken, StandardCharsets.UTF_8);

        Map<String, Object> vars = new HashMap<>();
        vars.put("userName", userName);
        vars.put("resetLink", resetLink);
        vars.put("email", email);

        String html = templateService.render(TEMPLATE, vars);

        dispatchService.dispatch(
                userId,
                NotificationType.PASSWORD_RESET,
                SUBJECT,
                "Запрошен сброс пароля. Если это были не вы — проигнорируйте письмо.",
                Map.of("userId", userId.toString()),
                TEMPLATE,
                SUBJECT,
                html);
    }
}
