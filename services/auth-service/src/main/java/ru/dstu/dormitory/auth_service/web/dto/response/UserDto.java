package ru.dstu.dormitory.auth_service.web.dto.response;

import ru.dstu.dormitory.auth_service.domain.model.RoleCode;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserDto(
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
