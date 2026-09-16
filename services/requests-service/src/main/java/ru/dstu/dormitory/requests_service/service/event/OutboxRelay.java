package ru.dstu.dormitory.requests_service.service.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.requests_service.config.KafkaTopicsProperties;
import ru.dstu.dormitory.requests_service.config.OutboxProperties;
import ru.dstu.dormitory.requests_service.domain.model.OutboxEvent;
import ru.dstu.dormitory.requests_service.domain.repo.OutboxEventRepository;
import ru.dstu.dormitory.requests_service.util.LogPatterns;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelay {

    private final OutboxEventRepository repository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final KafkaTopicsProperties topics;
    private final OutboxProperties outboxProperties;

    @Scheduled(fixedDelayString = "${outbox.relay.fixed-delay-ms:1000}")
    @Transactional
    public void relay() {
        int batch = outboxProperties.getBatchSize();
        List<OutboxEvent> unsent = repository.findUnsent(PageRequest.of(0, batch));
        if (unsent.isEmpty()) {
            return;
        }
        for (OutboxEvent event : unsent) {
            publish(event);
        }
    }

    private void publish(OutboxEvent event) {
        try {
            JsonNode payloadNode = objectMapper.readTree(event.getPayload());
            kafkaTemplate.send(topics.getRequestEvents(), event.getAggregateId(), wrap(event, payloadNode)).get();
            event.setSentAt(Instant.now());
            repository.save(event);
            log.info(LogPatterns.OUTBOX_PUBLISHED, event.getId(), event.getEventType());
        } catch (Exception ex) {
            log.error(LogPatterns.OUTBOX_PUBLISH_FAILED, event.getId(), ex.getMessage());
        }
    }

    private ObjectNode wrap(OutboxEvent event, JsonNode payloadNode) {
        ObjectNode envelope = objectMapper.createObjectNode();
        envelope.put("eventId", event.getId().toString());
        envelope.put("eventType", event.getEventType());
        envelope.put("aggregateType", event.getAggregateType());
        envelope.put("aggregateId", event.getAggregateId());
        envelope.put("occurredAt", event.getCreatedAt().toString());
        envelope.set("payload", payloadNode);
        return envelope;
    }
}
