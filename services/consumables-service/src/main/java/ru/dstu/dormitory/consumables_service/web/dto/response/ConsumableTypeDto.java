package ru.dstu.dormitory.consumables_service.web.dto.response;

import ru.dstu.dormitory.consumables_service.domain.enums.ConsumableUnit;

import java.time.Instant;
import java.util.UUID;

public record ConsumableTypeDto(
        UUID id,
        String name,
        ConsumableUnit unit,
        int stock,
        int lowStockThreshold,
        Instant createdAt,
        Instant updatedAt
) {
}
