package ru.dstu.dormitory.reports_service.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.reports_service.domain.enums.ApplianceStatus;
import ru.dstu.dormitory.reports_service.domain.model.ApplianceView;
import ru.dstu.dormitory.reports_service.domain.repo.ApplianceViewRepository;
import ru.dstu.dormitory.reports_service.domain.repo.UserViewRepository;
import ru.dstu.dormitory.reports_service.util.LogPatterns;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplianceRegisteredHandler implements EventHandler {

    public static final String TYPE = "ApplianceRegistered";

    private final ApplianceViewRepository repository;
    private final UserViewRepository userViewRepository;

    @Override
    public String eventType() {
        return TYPE;
    }

    @Override
    @Transactional
    public void handle(JsonNode payload) {
        UUID applianceId = UUID.fromString(payload.get("applianceId").asText());
        UUID residentId = UUID.fromString(payload.get("residentId").asText());
        UUID userId = UUID.fromString(payload.get("userId").asText());
        UUID roomId = payload.hasNonNull("roomId") ? UUID.fromString(payload.get("roomId").asText()) : null;
        String type = payload.get("type").asText();
        int powerWatts = payload.get("powerWatts").asInt();

        String residentName = userViewRepository.findById(userId)
                .map(u -> u.getFullName())
                .orElse(null);
        Instant now = Instant.now();

        ApplianceView view = repository.findById(applianceId)
                .orElseGet(() -> ApplianceView.builder().applianceId(applianceId).createdAt(now).build());
        view.setResidentId(residentId);
        view.setUserId(userId);
        view.setResidentName(residentName);
        view.setRoomId(roomId);
        view.setType(type);
        view.setPowerWatts(powerWatts);
        view.setStatus(ApplianceStatus.PENDING);
        view.setUpdatedAt(now);
        repository.save(view);

        log.info(LogPatterns.READ_MODEL_UPDATED, "appliances_view", applianceId);
    }
}
