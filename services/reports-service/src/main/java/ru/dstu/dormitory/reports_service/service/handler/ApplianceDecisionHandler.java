package ru.dstu.dormitory.reports_service.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.reports_service.domain.enums.ApplianceStatus;
import ru.dstu.dormitory.reports_service.domain.repo.ApplianceViewRepository;
import ru.dstu.dormitory.reports_service.util.LogPatterns;

import java.time.Instant;
import java.util.UUID;

/**
 * Базовое поведение для трёх событий-решений: Approved/Rejected/Revoked.
 */
@Slf4j
@RequiredArgsConstructor
abstract class ApplianceDecisionHandler implements EventHandler {

    private final ApplianceViewRepository repository;

    protected abstract ApplianceStatus targetStatus();

    @Override
    @Transactional
    public void handle(JsonNode payload) {
        UUID applianceId = UUID.fromString(payload.get("applianceId").asText());
        repository.findById(applianceId).ifPresent(view -> {
            view.setStatus(targetStatus());
            view.setUpdatedAt(Instant.now());
            repository.save(view);
            log.info(LogPatterns.READ_MODEL_UPDATED, "appliances_view", applianceId);
        });
    }
}
