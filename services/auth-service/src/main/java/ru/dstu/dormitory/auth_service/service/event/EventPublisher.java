package ru.dstu.dormitory.auth_service.service.event;

public interface EventPublisher {

    /**
     * Записывает событие в outbox в рамках текущей транзакции.
     * Отправку в Kafka выполняет {@link OutboxRelay}.
     */
    void publish(String aggregateType, String aggregateId, String eventType, Object payload);
}
