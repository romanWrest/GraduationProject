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
public class ResidentMovedHandler implements EventHandler {

    private static final String TEMPLATE = "resident-moved";

    private final TemplateService templateService;
    private final DispatchService dispatchService;
    private final AuthFacade authFacade;

    @Override
    public String eventType() {
        return "ResidentMoved";
    }

    @Override
    @LogMethod(value = "Обработка ResidentMoved")
    public void handle(JsonNode payload) {
        // ResidentMovedEvent содержит residentId, fromRoomId, toRoomId, movedAt.
        // userId по residentId — отдельный запрос. Поддержим оба варианта payload-а.
        UUID userId = HandlerSupport.extractUuid(payload, "userId");
        if (userId == null) {
            // Без userId уведомить мы не можем — пропускаем.
            return;
        }
        UUID toRoomId = HandlerSupport.extractUuid(payload, "toRoomId");
        String toRoomNumber = HandlerSupport.extractText(payload, "toRoomNumber");
        if (toRoomNumber == null) {
            toRoomNumber = toRoomId == null ? "—" : toRoomId.toString().substring(0, 8);
        }
        String movedAt = HandlerSupport.extractText(payload, "movedAt");

        String userName = HandlerSupport.userName(authFacade.getUser(userId));
        String subject = "Вы переселены в комнату %s".formatted(toRoomNumber);

        Map<String, Object> vars = new HashMap<>();
        vars.put("userName", userName);
        vars.put("toRoomNumber", toRoomNumber);
        vars.put("movedAt", movedAt);

        String html = templateService.render(TEMPLATE, vars);

        Map<String, Object> appPayload = new HashMap<>();
        if (toRoomId != null) {
            appPayload.put("toRoomId", toRoomId.toString());
        }
        appPayload.put("toRoomNumber", toRoomNumber);

        dispatchService.dispatch(
                userId,
                NotificationType.RESIDENT_MOVED,
                subject,
                "Вы переселены в комнату %s.".formatted(toRoomNumber),
                appPayload,
                TEMPLATE,
                subject,
                html);
    }
}
