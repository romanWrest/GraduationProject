package ru.dstu.dormitory.reports_service.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.reports_service.domain.model.UserView;
import ru.dstu.dormitory.reports_service.domain.repo.UserViewRepository;
import ru.dstu.dormitory.reports_service.service.LateJoinService;
import ru.dstu.dormitory.reports_service.util.LogPatterns;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCreatedHandler implements EventHandler {

    public static final String TYPE = "UserCreated";

    private final UserViewRepository repository;
    private final LateJoinService lateJoinService;

    @Override
    public String eventType() {
        return TYPE;
    }

    @Override
    @Transactional
    public void handle(JsonNode payload) {
        UUID userId = UUID.fromString(payload.get("userId").asText());
        String email = payload.hasNonNull("email") ? payload.get("email").asText() : null;
        String fullName = payload.hasNonNull("fullName") ? payload.get("fullName").asText() : null;

        UserView view = repository.findById(userId)
                .orElseGet(() -> UserView.builder().userId(userId).active(true).build());
        view.setEmail(email);
        view.setFullName(fullName);
        view.setActive(true);
        view.setUpdatedAt(Instant.now());
        repository.save(view);

        log.info(LogPatterns.READ_MODEL_UPDATED, "users_view", userId);

        // Поздний JOIN: дозаполняем имена в других read-моделях.
        lateJoinService.propagateUserName(userId, fullName);
    }
}
