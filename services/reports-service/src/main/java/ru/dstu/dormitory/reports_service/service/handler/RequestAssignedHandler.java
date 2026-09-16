package ru.dstu.dormitory.reports_service.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.reports_service.domain.enums.RequestStatus;
import ru.dstu.dormitory.reports_service.domain.repo.RequestViewRepository;
import ru.dstu.dormitory.reports_service.domain.repo.UserViewRepository;
import ru.dstu.dormitory.reports_service.util.LogPatterns;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestAssignedHandler implements EventHandler {

    public static final String TYPE = "RequestAssigned";

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
        UUID assigneeId = UUID.fromString(payload.get("assigneeId").asText());
        String assigneeName = userViewRepository.findById(assigneeId)
                .map(u -> u.getFullName())
                .orElse(null);

        repository.findById(requestId).ifPresent(view -> {
            view.setAssigneeId(assigneeId);
            view.setAssigneeName(assigneeName);
            if (view.getStatus() == RequestStatus.NEW || view.getStatus() == RequestStatus.IN_REVIEW) {
                view.setStatus(RequestStatus.ASSIGNED);
            }
            repository.save(view);
            log.info(LogPatterns.READ_MODEL_UPDATED, "requests_view", requestId);
        });
    }
}
