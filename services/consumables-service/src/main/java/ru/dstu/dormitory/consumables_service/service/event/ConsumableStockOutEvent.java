package ru.dstu.dormitory.consumables_service.service.event;

import java.util.UUID;

public record ConsumableStockOutEvent(
        UUID typeId,
        String name
) {
}
