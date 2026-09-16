package ru.dstu.dormitory.consumables_service.service.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.consumables_service.domain.enums.IssueStatus;
import ru.dstu.dormitory.consumables_service.domain.model.ConsumableIssue;
import ru.dstu.dormitory.consumables_service.domain.model.ProcessedEvent;
import ru.dstu.dormitory.consumables_service.domain.repo.ConsumableIssueRepository;
import ru.dstu.dormitory.consumables_service.domain.repo.ProcessedEventRepository;
import ru.dstu.dormitory.consumables_service.service.event.ConsumableReturnRequiredEvent;
import ru.dstu.dormitory.consumables_service.service.event.EventPublisher;
import ru.dstu.dormitory.consumables_service.service.event.EventTypes;
import ru.dstu.dormitory.consumables_service.util.LogPatterns;

import java.util.List;
import java.util.UUID;

/**
 * Консьюмер событий residents-service.
 *
 * <p>При {@code ResidentEvicted}: находит все активные ({@code ISSUED}) выдачи жильца
 * и публикует {@code ConsumableReturnRequired}, чтобы notifications-service уведомил
 * PROPERTY_MANAGER. Сами выдачи не закрываются — управляющий принимает возврат вручную.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResidentEventsConsumer {

    private static final String EVENT_RESIDENT_EVICTED = "ResidentEvicted";

    private final ConsumableIssueRepository issueRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final EventPublisher eventPublisher;

    @KafkaListener(topics = "${kafka.topics.resident-events:resident.events}",
            groupId = "${spring.kafka.consumer.group-id:consumables-service}",
            containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void onResidentEvent(JsonNode envelope) {
        if (envelope == null || !envelope.hasNonNull("eventType") || !envelope.hasNonNull("eventId")) {
            return;
        }
        String eventType = envelope.get("eventType").asText();
        if (!EVENT_RESIDENT_EVICTED.equals(eventType)) {
            return;
        }

        UUID eventId = UUID.fromString(envelope.get("eventId").asText());
        log.info(LogPatterns.EVENT_RECEIVED, eventType, eventId);

        if (processedEventRepository.existsById(eventId)) {
            log.info(LogPatterns.EVENT_DUPLICATE, eventId);
            return;
        }

        JsonNode payload = envelope.get("payload");
        if (payload == null) {
            return;
        }

        UUID residentId = extractUuid(payload, "residentId");
        UUID userId = extractUuid(payload, "userId");
        if (residentId == null && userId == null) {
            return;
        }

        List<ConsumableIssue> active = residentId != null
                ? issueRepository.findByResidentIdAndStatus(residentId, IssueStatus.ISSUED)
                : issueRepository.findByUserIdAndStatus(userId, IssueStatus.ISSUED);

        if (!active.isEmpty()) {
            UUID effectiveResidentId = residentId != null ? residentId : active.get(0).getResidentId();
            UUID effectiveUserId = userId != null ? userId : active.get(0).getUserId();
            List<UUID> issueIds = active.stream().map(ConsumableIssue::getId).toList();

            log.info(LogPatterns.ISSUE_AUTO_INITIATED_RETURN, effectiveResidentId, issueIds.size());

            eventPublisher.publish(EventTypes.AGGREGATE_ISSUE, effectiveResidentId.toString(),
                    EventTypes.CONSUMABLE_RETURN_REQUIRED,
                    new ConsumableReturnRequiredEvent(effectiveResidentId, effectiveUserId, issueIds));
        }

        processedEventRepository.save(ProcessedEvent.builder()
                .eventId(eventId)
                .eventType(eventType)
                .build());
        log.info(LogPatterns.EVENT_PROCESSED, eventType, eventId);
    }

    private UUID extractUuid(JsonNode payload, String field) {
        if (!payload.hasNonNull(field)) {
            return null;
        }
        try {
            return UUID.fromString(payload.get(field).asText());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
