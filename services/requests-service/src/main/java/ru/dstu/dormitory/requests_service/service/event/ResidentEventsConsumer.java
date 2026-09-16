package ru.dstu.dormitory.requests_service.service.event;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.requests_service.domain.enums.RequestStatus;
import ru.dstu.dormitory.requests_service.domain.model.Request;
import ru.dstu.dormitory.requests_service.domain.model.RequestStatusHistory;
import ru.dstu.dormitory.requests_service.domain.repo.RequestRepository;
import ru.dstu.dormitory.requests_service.domain.repo.RequestStatusHistoryRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Консьюмер событий residents-service.
 * <p>При {@code ResidentEvicted}:
 * <ul>
 *   <li>открытые заявки автора переводятся в {@code CANCELLED};</li>
 *   <li>заявки, где он был исполнителем, возвращаются в пул {@code IN_REVIEW}.</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResidentEventsConsumer {

    private static final String EVENT_RESIDENT_EVICTED = "ResidentEvicted";

    private final RequestRepository requestRepository;
    private final RequestStatusHistoryRepository historyRepository;
    private final EventPublisher eventPublisher;

    @KafkaListener(topics = "${kafka.topics.resident-events:resident.events}",
            groupId = "${spring.kafka.consumer.group-id:requests-service}",
            containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void onResidentEvent(JsonNode envelope) {
        log.info("Received: {}", envelope);
        if (envelope == null || !envelope.hasNonNull("eventType")) {
            return;
        }
        String eventType = envelope.get("eventType").asText();
        if (!EVENT_RESIDENT_EVICTED.equals(eventType)) {
            return;
        }
        JsonNode payload = envelope.get("payload");
        if (payload == null) {
            return;
        }

        UUID userId = extractUserId(payload);
        if (userId == null) {
            return;
        }

        log.info("Received resident event: ...");
        cancelAuthoredRequests(userId);
        reassignExecutorRequests(userId);
    }

    private UUID extractUserId(JsonNode payload) {
        if (payload.hasNonNull("userId")) {
            return UUID.fromString(payload.get("userId").asText());
        }
        if (payload.hasNonNull("residentUserId")) {
            return UUID.fromString(payload.get("residentUserId").asText());
        }
        return null;
    }

    private void cancelAuthoredRequests(UUID userId) {
        List<RequestStatus> open = List.of(
                RequestStatus.NEW,
                RequestStatus.IN_REVIEW,
                RequestStatus.ASSIGNED,
                RequestStatus.IN_PROGRESS,
                RequestStatus.DONE
        );
        List<Request> owned = requestRepository.findByAuthorAndStatuses(userId, open);
        for (Request r : owned) {
            RequestStatus from = r.getStatus();
            r.setStatus(RequestStatus.CANCELLED);
            r.setCancellationReason("Автор выселен");
            requestRepository.save(r);
            historyRepository.save(RequestStatusHistory.builder()
                    .request(r)
                    .fromStatus(from)
                    .toStatus(RequestStatus.CANCELLED)
                    .actorId(null)
                    .comment("Автор выселен")
                    .build());
            eventPublisher.publish(EventTypes.AGGREGATE_REQUEST, r.getId().toString(),
                    EventTypes.REQUEST_STATUS_CHANGED,
                    new RequestStatusChangedEvent(r.getId(), from, RequestStatus.CANCELLED,
                            null, Instant.now(), r.getType()));
        }
    }

    private void reassignExecutorRequests(UUID userId) {
        List<RequestStatus> active = List.of(RequestStatus.ASSIGNED, RequestStatus.IN_PROGRESS);
        List<Request> assigned = requestRepository.findByAssigneeAndStatuses(userId, active);
        for (Request r : assigned) {
            RequestStatus from = r.getStatus();
            r.setAssigneeId(null);
            r.setScheduledAt(null);
            r.setStatus(RequestStatus.IN_REVIEW);
            requestRepository.save(r);
            historyRepository.save(RequestStatusHistory.builder()
                    .request(r)
                    .fromStatus(from)
                    .toStatus(RequestStatus.IN_REVIEW)
                    .actorId(null)
                    .comment("Исполнитель выселен — возврат в пул")
                    .build());
            eventPublisher.publish(EventTypes.AGGREGATE_REQUEST, r.getId().toString(),
                    EventTypes.REQUEST_STATUS_CHANGED,
                    new RequestStatusChangedEvent(r.getId(), from, RequestStatus.IN_REVIEW,
                            null, Instant.now(), r.getType()));
        }
    }
}
