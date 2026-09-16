package ru.dstu.dormitory.auth_service.service.event;

import java.time.Instant;
import java.util.UUID;

public record PasswordResetRequestedEvent(
        UUID userId,
        String email,
        String resetToken,
        Instant expiresAt
) {
}
