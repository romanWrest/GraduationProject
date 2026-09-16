package ru.dstu.dormitory.consumables_service.service.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.consumables_service.domain.model.OutboxEvent;
import ru.dstu.dormitory.consumables_service.domain.repo.OutboxEventRepository;
import ru.dstu.dormitory.consumables_service.util.LogPatterns;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisherImpl implements EventPublisher {

    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void publish(String aggregateType, String aggregateId, String eventType, Object payload) {
        OutboxEvent event = OutboxEvent.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(serialize(payload))
                .build();
        OutboxEvent saved = repository.save(event);
        log.info(LogPatterns.OUTBOX_SAVED, saved.getId(), eventType);
    }

    private String serialize(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Не удалось сериализовать событие: " + ex.getMessage(), ex);
        }
    }
}
