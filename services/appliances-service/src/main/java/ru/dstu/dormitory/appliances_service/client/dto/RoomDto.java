package ru.dstu.dormitory.appliances_service.client.dto;

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
        Long occupied,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
}
