package ru.dstu.dormitory.notifications_service.security;

import ru.dstu.dormitory.notifications_service.domain.enums.RoleCode;

import java.util.Set;
import java.util.UUID;

public record UserPrincipal(
        UUID userId,
        String email,
        Set<RoleCode> roles
) {
}
