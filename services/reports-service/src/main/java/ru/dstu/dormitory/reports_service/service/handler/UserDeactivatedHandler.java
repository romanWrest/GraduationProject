package ru.dstu.dormitory.reports_service.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.reports_service.domain.repo.UserViewRepository;
import ru.dstu.dormitory.reports_service.util.LogPatterns;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDeactivatedHandler implements EventHandler {

    public static final String TYPE = "UserDeactivated";

    private final UserViewRepository repository;

    @Override
    public String eventType() {
        return TYPE;
    }

    @Override
    @Transactional
    public void handle(JsonNode payload) {
        UUID userId = UUID.fromString(payload.get("userId").asText());
        repository.findById(userId).ifPresent(view -> {
            view.setActive(false);
            view.setUpdatedAt(Instant.now());
            repository.save(view);
            log.info(LogPatterns.READ_MODEL_UPDATED, "users_view", userId);
        });
    }
}
