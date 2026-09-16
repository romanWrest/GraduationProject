package ru.dstu.dormitory.auth_service.web.dto.request;

import jakarta.validation.constraints.NotEmpty;
import ru.dstu.dormitory.auth_service.domain.model.RoleCode;

import java.util.Set;

public record UpdateUserRolesRequest(
        @NotEmpty Set<RoleCode> roles
) {
}
