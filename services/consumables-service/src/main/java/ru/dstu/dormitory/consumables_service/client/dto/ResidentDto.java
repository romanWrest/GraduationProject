package ru.dstu.dormitory.consumables_service.client.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO жильца из residents-service.
 */
public record ResidentDto(
        UUID id,
        UUID userId,
        String kind,
        String faculty,
        String studyGroup,
        String department,
        String phone,
        String contactInfo,
        UUID roomId,
        String roomNumber,
        LocalDate enrolledAt,
        LocalDate evictedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
