package ru.dstu.dormitory.notifications_service.web.dto;

import java.util.Map;

public record NotificationSettingsDto(
        boolean emailEnabled,
        boolean inAppEnabled,
        Map<String, Map<String, Boolean>> byType
) {
}
