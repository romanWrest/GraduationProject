package ru.dstu.dormitory.reports_service.service.handler;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Обработчик одного типа события. Тип объявляется через {@link #eventType()}.
 */
public interface EventHandler {

    String eventType();

    void handle(JsonNode payload);
}
