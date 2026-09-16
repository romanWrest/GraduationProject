package ru.dstu.dormitory.reports_service.service.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import ru.dstu.dormitory.reports_service.aspect.annotation.LogMethod;
import ru.dstu.dormitory.reports_service.service.IdempotencyService;
import ru.dstu.dormitory.reports_service.service.handler.EventHandler;
import ru.dstu.dormitory.reports_service.service.handler.HandlerRegistry;
import ru.dstu.dormitory.reports_service.util.EventEnvelope;
import ru.dstu.dormitory.reports_service.util.LogPatterns;

/**
 * Общая логика обработки события: парсинг envelope → idempotency → handler.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventDispatcher {

    private final ObjectMapper objectMapper;
    private final HandlerRegistry registry;
    private final IdempotencyService idempotencyService;

    @LogMethod(value = "Диспетчеризация Kafka-события", logArgs = {"topic"})
    public void dispatch(String topic, String rawJson) {
        long start = System.currentTimeMillis();
        EventEnvelope envelope;
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            envelope = EventEnvelope.from(root);
        } catch (Exception ex) {
            log.error(LogPatterns.EVENT_FAILED, topic, "<unknown>", ex.getMessage());
            throw new IllegalArgumentException("Битый envelope", ex);
        }

        MDC.put("eventId", envelope.eventId().toString());
        try {
            log.info(LogPatterns.EVENT_RECEIVED, topic, envelope.eventType(), envelope.eventId());

            if (idempotencyService.isProcessed(envelope.eventId())) {
                log.info(LogPatterns.EVENT_DUPLICATE, envelope.eventId());
                return;
            }

            EventHandler handler = registry.find(envelope.eventType()).orElse(null);
            if (handler == null) {
                log.info(LogPatterns.EVENT_SKIPPED, envelope.eventType(), envelope.eventId(), "no handler");
                idempotencyService.markProcessed(envelope.eventId(), envelope.eventType(), topic);
                return;
            }

            handler.handle(envelope.payload());
            idempotencyService.markProcessed(envelope.eventId(), envelope.eventType(), topic);
            log.info(LogPatterns.EVENT_PROCESSED, envelope.eventType(), envelope.eventId(),
                    System.currentTimeMillis() - start);
        } catch (RuntimeException ex) {
            log.error(LogPatterns.EVENT_FAILED, envelope.eventType(), envelope.eventId(), ex.getMessage());
            throw ex;
        } finally {
            MDC.remove("eventId");
        }
    }
}
