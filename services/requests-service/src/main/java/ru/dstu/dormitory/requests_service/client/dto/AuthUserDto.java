package ru.dstu.dormitory.requests_service.client.dto;

import ru.dstu.dormitory.requests_service.domain.enums.RoleCode;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record AuthUserDto(
        UUID id,
        String email,
        String fullName,
        String phone,
        boolean active,
        Set<RoleCode> roles,
        Instant createdAt,
        Instant updatedAt
) {
}
