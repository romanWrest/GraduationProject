package ru.dstu.dormitory.consumables_service.service.event;

import java.util.UUID;

public record ConsumableIssuedEvent(
        UUID issueId,
        UUID residentId,
        UUID userId,
        UUID typeId,
        String typeName,
        int quantity,
        UUID issuedBy
) {
}
