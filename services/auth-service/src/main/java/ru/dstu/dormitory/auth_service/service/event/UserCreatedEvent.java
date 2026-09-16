package ru.dstu.dormitory.auth_service.service.event;

import ru.dstu.dormitory.auth_service.domain.model.RoleCode;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserCreatedEvent(
        UUID userId,
        String email,
        String fullName,
        Set<RoleCode> roles,
        String temporaryPassword,
        Instant createdAt
) {
}
