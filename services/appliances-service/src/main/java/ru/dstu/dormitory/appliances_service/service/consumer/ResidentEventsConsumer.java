package ru.dstu.dormitory.appliances_service.service.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.appliances_service.domain.model.ProcessedEvent;
import ru.dstu.dormitory.appliances_service.domain.repo.ProcessedEventRepository;
import ru.dstu.dormitory.appliances_service.service.ApplianceService;
import ru.dstu.dormitory.appliances_service.util.LogPatterns;

import java.util.UUID;

/**
 * Консьюмер событий residents-service.
 *
 * <p>При {@code ResidentEvicted}: переводит все APPROVED-приборы выселяемого жильца
 * в REVOKED с reason="Жилец выселен" и публикует {@code ApplianceRevoked} с
 * {@code autoRevoked=true}. Идемпотентность — через {@code processed_events}.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResidentEventsConsumer {

    private static final String EVENT_RESIDENT_EVICTED = "ResidentEvicted";
    private static final String AUTO_REVOKE_REASON = "Жилец выселен";

    private final ApplianceService applianceService;
    private final ProcessedEventRepository processedEventRepository;

    @KafkaListener(topics = "${kafka.topics.resident-events:resident.events}",
            groupId = "${spring.kafka.consumer.group-id:appliances-service}",
            containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void onResidentEvent(JsonNode envelope) {
        log.info("Received {}", envelope);
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

        applianceService.autoRevokeForResident(residentId, userId, AUTO_REVOKE_REASON);

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
