package ru.dstu.dormitory.residents_service.web.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ResidencyHistoryDto(
        UUID id,
        UUID residentId,
        UUID roomId,
        String roomNumber,
        LocalDate movedInAt,
        LocalDate movedOutAt,
        String reason,
        Instant createdAt
) {
}
