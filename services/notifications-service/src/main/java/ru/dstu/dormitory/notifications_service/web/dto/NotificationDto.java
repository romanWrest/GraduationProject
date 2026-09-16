package ru.dstu.dormitory.notifications_service.web.dto;

import ru.dstu.dormitory.notifications_service.domain.enums.NotificationType;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record NotificationDto(
        UUID id,
        UUID userId,
        NotificationType type,
        String title,
        String body,
        Map<String, Object> payload,
        boolean read,
        Instant readAt,
        Instant createdAt
) {
}
