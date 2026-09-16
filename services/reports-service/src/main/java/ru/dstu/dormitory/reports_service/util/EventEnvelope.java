package ru.dstu.dormitory.reports_service.util;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

/**
 * Общий формат события: {@code { eventId, eventType, occurredAt, payload }}.
 */
public record EventEnvelope(UUID eventId, String eventType, Instant occurredAt, JsonNode payload) {

    public static EventEnvelope from(JsonNode root) {
        if (root == null || !root.hasNonNull("eventId") || !root.hasNonNull("eventType")) {
            throw new IllegalArgumentException("Битый envelope: отсутствуют обязательные поля eventId/eventType");
        }
        UUID eventId = UUID.fromString(root.get("eventId").asText());
        String eventType = root.get("eventType").asText();
        Instant occurredAt = root.hasNonNull("occurredAt")
                ? Instant.parse(root.get("occurredAt").asText())
                : Instant.now();
        JsonNode payload = root.get("payload");
        return new EventEnvelope(eventId, eventType, occurredAt, payload);
    }
}
