package ru.dstu.dormitory.residents_service.web.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record BatchResidentIdsRequest(
        @NotEmpty List<UUID> ids
) {
}
