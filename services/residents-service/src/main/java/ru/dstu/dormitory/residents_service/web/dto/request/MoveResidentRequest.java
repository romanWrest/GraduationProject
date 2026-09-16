package ru.dstu.dormitory.residents_service.web.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record MoveResidentRequest(
        @NotNull UUID newRoomId,
        @NotNull LocalDate movedAt,
        @Size(max = 512) String reason
) {
}
