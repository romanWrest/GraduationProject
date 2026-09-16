package ru.dstu.dormitory.residents_service.web.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RoomDetailDto(
        UUID id,
        String number,
        Short floor,
        Short capacity,
        long occupied,
        String notes,
        List<ResidentDto> residents,
        List<InventoryItemDto> inventory,
        Instant createdAt,
        Instant updatedAt
) {
}
