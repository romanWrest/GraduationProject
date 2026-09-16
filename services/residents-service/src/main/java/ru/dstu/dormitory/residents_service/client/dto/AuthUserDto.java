package ru.dstu.dormitory.residents_service.client.dto;

import ru.dstu.dormitory.residents_service.domain.enums.RoleCode;

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
