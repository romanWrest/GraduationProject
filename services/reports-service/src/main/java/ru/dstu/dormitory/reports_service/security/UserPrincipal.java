package ru.dstu.dormitory.reports_service.security;

import java.util.Set;
import java.util.UUID;

public record UserPrincipal(
        UUID userId,
        String email,
        Set<String> roles
) {
}
