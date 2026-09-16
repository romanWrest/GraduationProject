package ru.dstu.dormitory.reports_service.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.reports_service.domain.repo.ResidentViewRepository;
import ru.dstu.dormitory.reports_service.util.LogPatterns;

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResidentEvictedHandler implements EventHandler {

    public static final String TYPE = "ResidentEvicted";

    private final ResidentViewRepository repository;

    @Override
    public String eventType() {
        return TYPE;
    }

    @Override
    @Transactional
    public void handle(JsonNode payload) {
        UUID residentId = UUID.fromString(payload.get("residentId").asText());
        LocalDate evictedAt = LocalDate.parse(payload.get("evictedAt").asText());

        repository.findById(residentId).ifPresent(view -> {
            view.setEvictedAt(evictedAt);
            repository.save(view);
            log.info(LogPatterns.READ_MODEL_UPDATED, "residents_view", residentId);
        });
    }
}
