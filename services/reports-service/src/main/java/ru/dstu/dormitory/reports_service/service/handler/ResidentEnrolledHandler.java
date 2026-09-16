package ru.dstu.dormitory.reports_service.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.reports_service.domain.enums.ResidentKind;
import ru.dstu.dormitory.reports_service.domain.model.ResidentView;
import ru.dstu.dormitory.reports_service.domain.repo.ResidentViewRepository;
import ru.dstu.dormitory.reports_service.domain.repo.UserViewRepository;
import ru.dstu.dormitory.reports_service.util.LogPatterns;

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResidentEnrolledHandler implements EventHandler {

    public static final String TYPE = "ResidentEnrolled";

    private final ResidentViewRepository repository;
    private final UserViewRepository userViewRepository;

    @Override
    public String eventType() {
        return TYPE;
    }

    @Override
    @Transactional
    public void handle(JsonNode payload) {
        UUID residentId = UUID.fromString(payload.get("residentId").asText());
        UUID userId = UUID.fromString(payload.get("userId").asText());
        ResidentKind kind = ResidentKind.valueOf(payload.get("kind").asText());
        UUID roomId = payload.hasNonNull("roomId") ? UUID.fromString(payload.get("roomId").asText()) : null;
        LocalDate enrolledAt = LocalDate.parse(payload.get("enrolledAt").asText());

        String fullName = userViewRepository.findById(userId)
                .map(u -> u.getFullName())
                .orElse(null);

        ResidentView view = repository.findById(residentId)
                .orElseGet(() -> ResidentView.builder().residentId(residentId).build());
        view.setUserId(userId);
        view.setFullName(fullName);
        view.setKind(kind);
        view.setRoomId(roomId);
        view.setEnrolledAt(enrolledAt);
        view.setEvictedAt(null);
        repository.save(view);

        log.info(LogPatterns.READ_MODEL_UPDATED, "residents_view", residentId);
    }
}
