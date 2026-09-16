package ru.dstu.dormitory.requests_service.client.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO комнаты из residents-service.
 */
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
