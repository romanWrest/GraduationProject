package ru.dstu.dormitory.residents_service.web.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record EvictResidentRequest(
        @NotNull LocalDate evictedAt,
        @Size(max = 512) String reason
) {
}
