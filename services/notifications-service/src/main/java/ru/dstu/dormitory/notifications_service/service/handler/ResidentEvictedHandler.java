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
public class ResidentEvictedHandler implements EventHandler {

    private static final String TEMPLATE = "resident-evicted";
    private static final String SUBJECT = "Выселение из общежития";

    private final TemplateService templateService;
    private final DispatchService dispatchService;
    private final AuthFacade authFacade;

    @Override
    public String eventType() {
        return "ResidentEvicted";
    }

    @Override
    @LogMethod(value = "Обработка ResidentEvicted")
    public void handle(JsonNode payload) {
        UUID userId = HandlerSupport.extractUuid(payload, "userId");
        if (userId == null) {
            return;
        }
        String evictedAt = HandlerSupport.extractText(payload, "evictedAt");
        String reason = HandlerSupport.extractText(payload, "reason");

        String userName = HandlerSupport.userName(authFacade.getUser(userId));

        Map<String, Object> vars = new HashMap<>();
        vars.put("userName", userName);
        vars.put("evictedAt", evictedAt);
        vars.put("reason", reason);

        String html = templateService.render(TEMPLATE, vars);

        Map<String, Object> appPayload = new HashMap<>();
        if (reason != null) {
            appPayload.put("reason", reason);
        }

        dispatchService.dispatch(
                userId,
                NotificationType.RESIDENT_EVICTED,
                SUBJECT,
                "Вы выселены из общежития.",
                appPayload,
                TEMPLATE,
                SUBJECT,
                html);
    }
}
