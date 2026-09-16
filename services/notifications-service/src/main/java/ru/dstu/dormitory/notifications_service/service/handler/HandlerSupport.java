package ru.dstu.dormitory.notifications_service.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.experimental.UtilityClass;
import ru.dstu.dormitory.notifications_service.client.dto.AuthUserDto;

import java.util.UUID;

/**
 * Общие утилиты handler-ов.
 */
@UtilityClass
public class HandlerSupport {

    /** Извлечь UUID из payload-а; null, если поля нет либо значение пустое. */
    public UUID extractUuid(JsonNode payload, String field) {
        if (payload == null || !payload.hasNonNull(field)) {
            return null;
        }
        String text = payload.get(field).asText().trim();
        if (text.isEmpty()) {
            return null;
        }
        return UUID.fromString(text);
    }

    public String extractText(JsonNode payload, String field) {
        if (payload == null || !payload.hasNonNull(field)) {
            return null;
        }
        return payload.get(field).asText();
    }

    public String userName(AuthUserDto user) {
        if (user == null) {
            return "пользователь";
        }
        return user.fullName() != null && !user.fullName().isBlank() ? user.fullName() : user.email();
    }

    /** Короткий ID (8 первых символов UUID) для отображения в темах писем. */
    public String shortId(UUID id) {
        return id == null ? "" : id.toString().substring(0, 8);
    }
}
