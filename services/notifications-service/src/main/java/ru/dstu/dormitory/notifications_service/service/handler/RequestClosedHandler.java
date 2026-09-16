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
public class RequestClosedHandler implements EventHandler {

    private static final String TEMPLATE = "request-closed";

    private final TemplateService templateService;
    private final DispatchService dispatchService;
    private final AuthFacade authFacade;
    private final AppFrontendProperties frontend;

    @Override
    public String eventType() {
        return "RequestClosed";
    }

    @Override
    @LogMethod(value = "Обработка RequestClosed")
    public void handle(JsonNode payload) {
        UUID requestId = HandlerSupport.extractUuid(payload, "requestId");
        UUID authorId = HandlerSupport.extractUuid(payload, "authorId");
        if (requestId == null || authorId == null) {
            return;
        }
        boolean autoClosed = payload != null && payload.hasNonNull("autoClosed")
                && payload.get("autoClosed").asBoolean(false);

        String shortId = HandlerSupport.shortId(requestId);
        String subject = "Заявка #%s закрыта".formatted(shortId);
        String actionUrl = frontend.getBaseUrl() + "/requests/" + requestId;

        String userName = HandlerSupport.userName(authFacade.getUser(authorId));

        Map<String, Object> vars = new HashMap<>();
        vars.put("userName", userName);
        vars.put("requestShortId", shortId);
        vars.put("autoClosed", autoClosed);
        vars.put("actionUrl", actionUrl);

        String html = templateService.render(TEMPLATE, vars);

        Map<String, Object> appPayload = new HashMap<>();
        appPayload.put("requestId", requestId.toString());
        appPayload.put("autoClosed", autoClosed);

        dispatchService.dispatch(
                authorId,
                NotificationType.REQUEST_CLOSED,
                subject,
                "Заявка #%s закрыта.".formatted(shortId),
                appPayload,
                TEMPLATE,
                subject,
                html);
    }
}
