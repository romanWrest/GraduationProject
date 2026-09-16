package ru.dstu.dormitory.consumables_service.service.event;

import ru.dstu.dormitory.consumables_service.domain.enums.ReturnCondition;

import java.util.UUID;

public record ConsumableReturnedEvent(
        UUID issueId,
        UUID residentId,
        UUID userId,
        UUID typeId,
        ReturnCondition condition,
        UUID returnedBy
) {
}
