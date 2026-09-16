package ru.dstu.dormitory.reports_service.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.reports_service.domain.enums.ConsumableStatus;
import ru.dstu.dormitory.reports_service.domain.model.ConsumableView;
import ru.dstu.dormitory.reports_service.domain.repo.ConsumableViewRepository;
import ru.dstu.dormitory.reports_service.domain.repo.UserViewRepository;
import ru.dstu.dormitory.reports_service.util.LogPatterns;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsumableIssuedHandler implements EventHandler {

    public static final String TYPE = "ConsumableIssued";

    private final ConsumableViewRepository repository;
    private final UserViewRepository userViewRepository;

    @Override
    public String eventType() {
        return TYPE;
    }

    @Override
    @Transactional
    public void handle(JsonNode payload) {
        UUID issueId = UUID.fromString(payload.get("issueId").asText());
        UUID residentId = UUID.fromString(payload.get("residentId").asText());
        UUID userId = UUID.fromString(payload.get("userId").asText());
        UUID typeId = UUID.fromString(payload.get("typeId").asText());
        String typeName = payload.hasNonNull("typeName") ? payload.get("typeName").asText() : null;
        int quantity = payload.get("quantity").asInt();

        String residentName = userViewRepository.findById(userId)
                .map(u -> u.getFullName())
                .orElse(null);

        ConsumableView view = repository.findById(issueId)
                .orElseGet(() -> ConsumableView.builder().issueId(issueId).build());
        view.setResidentId(residentId);
        view.setUserId(userId);
        view.setResidentName(residentName);
        view.setTypeId(typeId);
        view.setTypeName(typeName);
        view.setQuantity(quantity);
        view.setStatus(ConsumableStatus.ISSUED);
        if (view.getIssuedAt() == null) {
            view.setIssuedAt(Instant.now());
        }
        repository.save(view);

        log.info(LogPatterns.READ_MODEL_UPDATED, "consumables_view", issueId);
    }
}
