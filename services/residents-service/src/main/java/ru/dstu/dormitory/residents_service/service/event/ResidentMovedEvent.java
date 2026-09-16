package ru.dstu.dormitory.residents_service.service.event;

import java.time.LocalDate;
import java.util.UUID;

public record ResidentMovedEvent(
        UUID residentId,
        UUID fromRoomId,
        UUID toRoomId,
        LocalDate movedAt
) {
}
