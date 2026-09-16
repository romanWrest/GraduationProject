package ru.dstu.dormitory.reports_service.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.reports_service.domain.enums.RequestStatus;
import ru.dstu.dormitory.reports_service.domain.repo.RequestViewRepository;
import ru.dstu.dormitory.reports_service.util.LogPatterns;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestStatusChangedHandler implements EventHandler {

    public static final String TYPE = "RequestStatusChanged";

    private final RequestViewRepository repository;

    @Override
    public String eventType() {
        return TYPE;
    }

    @Override
    @Transactional
    public void handle(JsonNode payload) {
        UUID requestId = UUID.fromString(payload.get("requestId").asText());
        RequestStatus toStatus = RequestStatus.valueOf(payload.get("toStatus").asText());

        repository.findById(requestId).ifPresent(view -> {
            view.setStatus(toStatus);
            repository.save(view);
            log.info(LogPatterns.READ_MODEL_UPDATED, "requests_view", requestId);
        });
    }
}
