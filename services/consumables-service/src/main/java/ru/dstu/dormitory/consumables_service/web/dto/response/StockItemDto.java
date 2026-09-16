package ru.dstu.dormitory.consumables_service.web.dto.response;

import ru.dstu.dormitory.consumables_service.domain.enums.ConsumableUnit;

import java.util.UUID;

public record StockItemDto(
        UUID typeId,
        String name,
        ConsumableUnit unit,
        int stock,
        int lowStockThreshold,
        boolean low
) {
}
