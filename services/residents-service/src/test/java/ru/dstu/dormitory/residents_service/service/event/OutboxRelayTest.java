package ru.dstu.dormitory.residents_service.service.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import ru.dstu.dormitory.residents_service.config.KafkaTopicsProperties;
import ru.dstu.dormitory.residents_service.config.OutboxProperties;
import ru.dstu.dormitory.residents_service.domain.model.OutboxEvent;
import ru.dstu.dormitory.residents_service.domain.repo.OutboxEventRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxRelayTest {

    @Mock
    private OutboxEventRepository repository;
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Mock
    private KafkaTopicsProperties topics;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OutboxProperties outboxProperties = new OutboxProperties();

    private OutboxRelay relay;

    @BeforeEach
    void setUp() {
        relay = new OutboxRelay(repository, kafkaTemplate, objectMapper, topics, outboxProperties);
    }

    @Test
    @DisplayName("Нет несформированных событий — ничего не делает")
    void relay_noEvents() {
        when(repository.findUnsent(any(Pageable.class))).thenReturn(List.of());
        relay.relay();
        verify(kafkaTemplate, never()).send(anyString(), any(), any());
    }

    @Test
    @DisplayName("Отправляет событие и проставляет sent_at")
    void relay_publishesAndMarksSent() {
        OutboxEvent event = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .aggregateType("resident")
                .aggregateId(UUID.randomUUID().toString())
                .eventType("ResidentEnrolled")
                .payload("{\"x\":1}")
                .createdAt(Instant.now())
                .build();
        when(repository.findUnsent(any(Pageable.class))).thenReturn(List.of(event));
        when(topics.getResidentEvents()).thenReturn("resident.events");

        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(eq("resident.events"), eq(event.getAggregateId()), any(ObjectNode.class)))
                .thenReturn(future);

        relay.relay();

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getSentAt()).isNotNull();
    }

    @Test
    @DisplayName("Ошибка Kafka не падает, sent_at не проставляется")
    void relay_kafkaFailure() {
        OutboxEvent event = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .aggregateType("resident")
                .aggregateId(UUID.randomUUID().toString())
                .eventType("ResidentEnrolled")
                .payload("{\"x\":1}")
                .createdAt(Instant.now())
                .build();
        when(repository.findUnsent(any(Pageable.class))).thenReturn(List.of(event));
        when(topics.getResidentEvents()).thenReturn("resident.events");

        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.failedFuture(
                new RuntimeException("boom"));
        when(kafkaTemplate.send(anyString(), anyString(), any(ObjectNode.class))).thenReturn(future);

        relay.relay();

        verify(repository, never()).save(any());
    }
}
