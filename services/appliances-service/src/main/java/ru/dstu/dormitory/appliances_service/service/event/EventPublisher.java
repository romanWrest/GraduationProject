package ru.dstu.dormitory.appliances_service.service.event;

public interface EventPublisher {

    /**
     * Сохраняет событие в outbox в рамках текущей транзакции.
     * Асинхронная отправка в Kafka выполняется {@link OutboxRelay}.
     */
    void publish(String aggregateType, String aggregateId, String eventType, Object payload);
}
