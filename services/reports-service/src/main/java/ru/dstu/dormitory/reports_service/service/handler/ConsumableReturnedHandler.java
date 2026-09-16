package ru.dstu.dormitory.reports_service.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.reports_service.domain.enums.ConsumableStatus;
import ru.dstu.dormitory.reports_service.domain.enums.ReturnCondition;
import ru.dstu.dormitory.reports_service.domain.repo.ConsumableViewRepository;
import ru.dstu.dormitory.reports_service.util.LogPatterns;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsumableReturnedHandler implements EventHandler {

    public static final String TYPE = "ConsumableReturned";

    private final ConsumableViewRepository repository;

    @Override
    public String eventType() {
        return TYPE;
    }

    @Override
    @Transactional
    public void handle(JsonNode payload) {
        UUID issueId = UUID.fromString(payload.get("issueId").asText());
        ReturnCondition condition = payload.hasNonNull("condition")
                ? ReturnCondition.valueOf(payload.get("condition").asText())
                : ReturnCondition.OK;

        repository.findById(issueId).ifPresent(view -> {
            view.setReturnedAt(Instant.now());
            view.setReturnCondition(condition);
            view.setStatus(condition == ReturnCondition.LOST
                    ? ConsumableStatus.LOST
                    : ConsumableStatus.RETURNED);
            repository.save(view);
            log.info(LogPatterns.READ_MODEL_UPDATED, "consumables_view", issueId);
        });
    }
}
