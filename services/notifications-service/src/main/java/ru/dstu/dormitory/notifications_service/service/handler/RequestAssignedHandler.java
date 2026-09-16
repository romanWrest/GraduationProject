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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RequestAssignedHandler implements EventHandler {

    private static final String TEMPLATE = "request-assigned";

    private final TemplateService templateService;
    private final DispatchService dispatchService;
    private final AuthFacade authFacade;
    private final AppFrontendProperties frontend;

    @Override
    public String eventType() {
        return "RequestAssigned";
    }

    @Override
    @LogMethod(value = "Обработка RequestAssigned")
    public void handle(JsonNode payload) {
        UUID requestId = HandlerSupport.extractUuid(payload, "requestId");
        if (requestId == null) {
            return;
        }
        UUID assigneeId = HandlerSupport.extractUuid(payload, "assigneeId");
        UUID authorId = HandlerSupport.extractUuid(payload, "authorId");
        String scheduledAt = HandlerSupport.extractText(payload, "scheduledAt");

        String shortId = HandlerSupport.shortId(requestId);
        String subject = "Заявка #%s назначена".formatted(shortId);
        String actionUrl = frontend.getBaseUrl() + "/requests/" + requestId;

        Map<String, Object> appPayload = new HashMap<>();
        appPayload.put("requestId", requestId.toString());

        Set<UUID> recipients = new HashSet<>();
        if (assigneeId != null) {
            recipients.add(assigneeId);
        }
        if (authorId != null) {
            recipients.add(authorId);
        }

        for (UUID recipient : recipients) {
            AuthUserDto user = authFacade.getUser(recipient);
            String userName = HandlerSupport.userName(user);

            Map<String, Object> vars = new HashMap<>();
            vars.put("userName", userName);
            vars.put("requestShortId", shortId);
            vars.put("scheduledAt", scheduledAt);
            vars.put("actionUrl", actionUrl);

            String html = templateService.render(TEMPLATE, vars);

            dispatchService.dispatch(
                    recipient,
                    NotificationType.REQUEST_ASSIGNED,
                    subject,
                    "Заявка #%s назначена исполнителю.".formatted(shortId),
                    appPayload,
                    TEMPLATE,
                    subject,
                    html);
        }
    }
}
