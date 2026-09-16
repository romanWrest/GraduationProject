package ru.dstu.dormitory.requests_service.service.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import ru.dstu.dormitory.requests_service.config.KafkaTopicsProperties;
import ru.dstu.dormitory.requests_service.config.OutboxProperties;
import ru.dstu.dormitory.requests_service.domain.model.OutboxEvent;
import ru.dstu.dormitory.requests_service.domain.repo.OutboxEventRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxRelayTest {

    @Mock
    OutboxEventRepository repository;
    @Mock
    KafkaTemplate<String, Object> kafkaTemplate;

    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    KafkaTopicsProperties topics = new KafkaTopicsProperties();
    OutboxProperties outboxProperties = new OutboxProperties();

    OutboxRelay relay;

    @BeforeEach
    void setUp() {
        relay = new OutboxRelay(repository, kafkaTemplate, objectMapper, topics, outboxProperties);
    }

    @Test
    void publishesUnsentEventsAndMarksSentAt() throws Exception {
        OutboxEvent event = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .aggregateType("request")
                .aggregateId(UUID.randomUUID().toString())
                .eventType("RequestCreated")
                .payload("{\"a\":1}")
                .createdAt(Instant.now())
                .build();

        when(repository.findUnsent(any(PageRequest.class))).thenReturn(List.of(event));
        when(kafkaTemplate.send(eq(topics.getRequestEvents()), anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        relay.relay();

        verify(repository).save(event);
        assertNotNull(event.getSentAt());
    }

    @Test
    void keepsSentAtNullOnKafkaFailure() {
        OutboxEvent event = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .aggregateType("request")
                .aggregateId(UUID.randomUUID().toString())
                .eventType("RequestCreated")
                .payload("{\"a\":1}")
                .createdAt(Instant.now())
                .build();

        when(repository.findUnsent(any(PageRequest.class))).thenReturn(List.of(event));
        CompletableFuture<Object> failed = new CompletableFuture<>();
        failed.completeExceptionally(new RuntimeException("Kafka down"));
        when(kafkaTemplate.send(eq(topics.getRequestEvents()), anyString(), any()))
                .thenReturn((CompletableFuture) failed);

        relay.relay();

        assertNull(event.getSentAt());
        verify(repository, never()).save(event);
    }
}
