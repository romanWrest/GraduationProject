package ru.dstu.dormitory.auth_service.security;

import ru.dstu.dormitory.auth_service.domain.model.RoleCode;

import java.util.Set;
import java.util.UUID;

public record UserPrincipal(
        UUID userId,
        String email,
        Set<RoleCode> roles
) {
}
