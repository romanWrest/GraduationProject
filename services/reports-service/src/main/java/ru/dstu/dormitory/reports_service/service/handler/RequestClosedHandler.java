package ru.dstu.dormitory.reports_service.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.reports_service.domain.enums.RequestStatus;
import ru.dstu.dormitory.reports_service.domain.repo.RequestViewRepository;
import ru.dstu.dormitory.reports_service.util.LogPatterns;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestClosedHandler implements EventHandler {

    public static final String TYPE = "RequestClosed";

    private final RequestViewRepository repository;

    @Override
    public String eventType() {
        return TYPE;
    }

    @Override
    @Transactional
    public void handle(JsonNode payload) {
        UUID requestId = UUID.fromString(payload.get("requestId").asText());
        Instant closedAt = Instant.parse(payload.get("closedAt").asText());
        boolean autoClosed = payload.hasNonNull("autoClosed") && payload.get("autoClosed").asBoolean();

        repository.findById(requestId).ifPresent(view -> {
            view.setStatus(RequestStatus.CLOSED);
            view.setClosedAt(closedAt);
            view.setAutoClosed(autoClosed);
            if (view.getCreatedAt() != null) {
                view.setResolutionSeconds(closedAt.getEpochSecond() - view.getCreatedAt().getEpochSecond());
            }
            repository.save(view);
            log.info(LogPatterns.READ_MODEL_UPDATED, "requests_view", requestId);
        });
    }
}
