package ru.dstu.dormitory.residents_service.service.event;

import java.time.LocalDate;
import java.util.UUID;

public record ResidentEvictedEvent(
        UUID residentId,
        UUID userId,
        UUID roomId,
        LocalDate evictedAt,
        String reason
) {
}
