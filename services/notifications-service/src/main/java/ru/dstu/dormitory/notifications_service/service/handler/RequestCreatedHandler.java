package ru.dstu.dormitory.notifications_service.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.dstu.dormitory.notifications_service.aspect.annotation.LogMethod;
import ru.dstu.dormitory.notifications_service.client.AuthFacade;
import ru.dstu.dormitory.notifications_service.client.dto.AuthUserDto;
import ru.dstu.dormitory.notifications_service.config.AppFrontendProperties;
import ru.dstu.dormitory.notifications_service.domain.enums.NotificationType;
import ru.dstu.dormitory.notifications_service.domain.enums.RoleCode;
import ru.dstu.dormitory.notifications_service.service.DispatchService;
import ru.dstu.dormitory.notifications_service.service.TemplateService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RequestCreatedHandler implements EventHandler {

    private static final String TEMPLATE = "request-created";

    private final TemplateService templateService;
    private final DispatchService dispatchService;
    private final AuthFacade authFacade;
    private final AppFrontendProperties frontend;

    @Override
    public String eventType() {
        return "RequestCreated";
    }

    @Override
    @LogMethod(value = "Обработка RequestCreated")
    public void handle(JsonNode payload) {
        UUID requestId = HandlerSupport.extractUuid(payload, "requestId");
        UUID authorId = HandlerSupport.extractUuid(payload, "authorId");
        if (requestId == null || authorId == null) {
            return;
        }
        String requestType = HandlerSupport.extractText(payload, "type");
        String requestTitle = HandlerSupport.extractText(payload, "title");
        String shortId = HandlerSupport.shortId(requestId);
        String subject = "Заявка #%s создана".formatted(shortId);
        String actionUrl = frontend.getBaseUrl() + "/requests/" + requestId;

        Map<String, Object> appPayload = new HashMap<>();
        appPayload.put("requestId", requestId.toString());
        if (requestType != null) {
            appPayload.put("type", requestType);
        }

        Set<UUID> recipients = new HashSet<>();
        recipients.add(authorId);
        for (AuthUserDto admin : authFacade.getUsersByRole(RoleCode.ADMIN.name())) {
            if (admin.id() != null) {
                recipients.add(admin.id());
            }
        }

        for (UUID recipient : recipients) {
            AuthUserDto user = authFacade.getUser(recipient);
            String userName = HandlerSupport.userName(user);

            Map<String, Object> vars = new HashMap<>();
            vars.put("userName", userName);
            vars.put("requestShortId", shortId);
            vars.put("requestType", requestType);
            vars.put("requestTitle", requestTitle);
            vars.put("actionUrl", actionUrl);

            String html = templateService.render(TEMPLATE, vars);

            dispatchService.dispatch(
                    recipient,
                    NotificationType.REQUEST_CREATED,
                    subject,
                    "Создана новая заявка #%s.".formatted(shortId),
                    appPayload,
                    TEMPLATE,
                    subject,
                    html);
        }
    }
}
