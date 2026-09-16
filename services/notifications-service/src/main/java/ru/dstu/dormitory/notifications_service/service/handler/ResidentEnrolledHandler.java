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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ResidentEnrolledHandler implements EventHandler {

    private static final String TEMPLATE = "resident-enrolled";

    private final TemplateService templateService;
    private final DispatchService dispatchService;
    private final AuthFacade authFacade;
    private final AppFrontendProperties frontend;

    @Override
    public String eventType() {
        return "ResidentEnrolled";
    }

    @Override
    @LogMethod(value = "Обработка ResidentEnrolled")
    public void handle(JsonNode payload) {
        UUID userId = HandlerSupport.extractUuid(payload, "userId");
        if (userId == null) {
            return;
        }
        UUID roomId = HandlerSupport.extractUuid(payload, "roomId");
        String roomNumber = HandlerSupport.extractText(payload, "roomNumber");
        if (roomNumber == null) {
            roomNumber = roomId == null ? "—" : roomId.toString().substring(0, 8);
        }
        String enrolledAt = HandlerSupport.extractText(payload, "enrolledAt");

        String userName = HandlerSupport.userName(authFacade.getUser(userId));
        String subject = "Вы заселены в комнату %s".formatted(roomNumber);

        Map<String, Object> vars = new HashMap<>();
        vars.put("userName", userName);
        vars.put("roomNumber", roomNumber);
        vars.put("enrolledAt", enrolledAt);
        vars.put("actionUrl", frontend.getBaseUrl() + "/profile");

        String html = templateService.render(TEMPLATE, vars);

        Map<String, Object> appPayload = new HashMap<>();
        if (roomId != null) {
            appPayload.put("roomId", roomId.toString());
        }
        appPayload.put("roomNumber", roomNumber);

        dispatchService.dispatch(
                userId,
                NotificationType.RESIDENT_ENROLLED,
                subject,
                "Вы заселены в комнату %s.".formatted(roomNumber),
                appPayload,
                TEMPLATE,
                subject,
                html);
    }
}
