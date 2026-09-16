package ru.dstu.dormitory.residents_service.client.dto;

import java.util.List;
import java.util.UUID;

public record BatchUserIdsRequest(
        List<UUID> ids
) {
}
