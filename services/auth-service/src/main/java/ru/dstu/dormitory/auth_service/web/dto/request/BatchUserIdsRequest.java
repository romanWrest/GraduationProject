package ru.dstu.dormitory.auth_service.web.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record BatchUserIdsRequest(
        @NotEmpty List<UUID> ids
) {
}
