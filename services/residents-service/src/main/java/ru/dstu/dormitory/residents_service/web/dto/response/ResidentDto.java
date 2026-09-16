package ru.dstu.dormitory.residents_service.web.dto.response;

import ru.dstu.dormitory.residents_service.domain.enums.ResidentKind;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ResidentDto(
        UUID id,
        UUID userId,
        ResidentKind kind,
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
