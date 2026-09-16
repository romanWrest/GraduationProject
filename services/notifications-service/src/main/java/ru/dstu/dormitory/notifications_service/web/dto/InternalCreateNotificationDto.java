package ru.dstu.dormitory.notifications_service.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import ru.dstu.dormitory.notifications_service.domain.enums.NotificationChannel;
import ru.dstu.dormitory.notifications_service.domain.enums.NotificationType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record InternalCreateNotificationDto(
        @NotNull UUID userId,
        @NotNull NotificationType type,
        @NotBlank String title,
        @NotBlank String body,
        Map<String, Object> payload,
        @NotEmpty List<NotificationChannel> channels
) {
}
