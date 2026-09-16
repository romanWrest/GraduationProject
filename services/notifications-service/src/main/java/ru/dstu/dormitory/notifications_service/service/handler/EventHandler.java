package ru.dstu.dormitory.notifications_service.service.handler;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Обработчик события одного типа.
 */
public interface EventHandler {

    /** Возвращает имя обрабатываемого {@code eventType} (например, "UserCreated"). */
    String eventType();

    void handle(JsonNode payload);
}
