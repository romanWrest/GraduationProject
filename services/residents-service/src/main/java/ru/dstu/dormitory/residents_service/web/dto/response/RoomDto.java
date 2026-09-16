package ru.dstu.dormitory.residents_service.web.dto.response;

import java.time.Instant;
import java.util.UUID;

public record RoomDto(
        UUID id,
        String number,
        Short floor,
        Short capacity,
        long occupied,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
}
