package ru.dstu.dormitory.residents_service.web.dto.response;

import ru.dstu.dormitory.residents_service.domain.enums.InventoryState;
import ru.dstu.dormitory.residents_service.domain.enums.InventoryType;

import java.time.Instant;
import java.util.UUID;

public record InventoryItemDto(
        UUID id,
        UUID roomId,
        InventoryType type,
        String serialNumber,
        InventoryState state,
        String notes,
        Instant writtenOffAt,
        String writtenOffReason,
        Instant createdAt,
        Instant updatedAt
) {
}
