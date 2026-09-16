package ru.dstu.dormitory.reports_service.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.reports_service.domain.enums.RequestStatus;
import ru.dstu.dormitory.reports_service.domain.enums.RequestType;
import ru.dstu.dormitory.reports_service.domain.enums.TargetPool;
import ru.dstu.dormitory.reports_service.domain.model.RequestView;
import ru.dstu.dormitory.reports_service.domain.repo.RequestViewRepository;
import ru.dstu.dormitory.reports_service.domain.repo.UserViewRepository;
import ru.dstu.dormitory.reports_service.util.LogPatterns;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestCreatedHandler implements EventHandler {

    public static final String TYPE = "RequestCreated";

    private final RequestViewRepository repository;
    private final UserViewRepository userViewRepository;

    @Override
    public String eventType() {
        return TYPE;
    }

    @Override
    @Transactional
    public void handle(JsonNode payload) {
        UUID requestId = UUID.fromString(payload.get("requestId").asText());
        RequestType type = RequestType.valueOf(payload.get("type").asText());
        UUID authorId = UUID.fromString(payload.get("authorId").asText());
        UUID roomId = payload.hasNonNull("roomId") ? UUID.fromString(payload.get("roomId").asText()) : null;
        TargetPool targetPool = payload.hasNonNull("targetPool")
                ? TargetPool.valueOf(payload.get("targetPool").asText())
                : null;
        Instant createdAt = Instant.parse(payload.get("createdAt").asText());

        String authorName = userViewRepository.findById(authorId)
                .map(u -> u.getFullName())
                .orElse(null);

        RequestView view = repository.findById(requestId)
                .orElseGet(() -> RequestView.builder().requestId(requestId).build());
        view.setType(type);
        view.setStatus(RequestStatus.NEW);
        view.setAuthorId(authorId);
        view.setAuthorName(authorName);
        view.setRoomId(roomId);
        view.setTargetPool(targetPool);
        view.setCreatedAt(createdAt);
        repository.save(view);

        log.info(LogPatterns.READ_MODEL_UPDATED, "requests_view", requestId);
    }
}
