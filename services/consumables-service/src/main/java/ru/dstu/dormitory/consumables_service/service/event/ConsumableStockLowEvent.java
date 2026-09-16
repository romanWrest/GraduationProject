package ru.dstu.dormitory.consumables_service.service.event;

import java.util.UUID;

public record ConsumableStockLowEvent(
        UUID typeId,
        String name,
        int stock,
        int threshold
) {
}
