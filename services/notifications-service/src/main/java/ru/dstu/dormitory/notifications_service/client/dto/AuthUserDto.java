package ru.dstu.dormitory.notifications_service.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ru.dstu.dormitory.notifications_service.domain.enums.RoleCode;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AuthUserDto(
        UUID id,
        String email,
        String fullName,
        String phone,
        boolean active,
        Set<RoleCode> roles,
        Instant createdAt,
        Instant updatedAt
) implements Serializable {
}
