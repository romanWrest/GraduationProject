package ru.dstu.dormitory.notifications_service.web.dto;

import java.util.Map;

public record UpdateSettingsDto(
        Boolean emailEnabled,
        Boolean inAppEnabled,
        Map<String, Map<String, Boolean>> byType
) {
}
