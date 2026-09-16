package ru.dstu.dormitory.consumables_service.service.event;

import java.util.List;
import java.util.UUID;

/**
 * Уведомление PROPERTY_MANAGER о необходимости принять возврат расходников
 * у выселяемого жильца.
 */
public record ConsumableReturnRequiredEvent(
        UUID residentId,
        UUID userId,
        List<UUID> issueIds
) {
}
