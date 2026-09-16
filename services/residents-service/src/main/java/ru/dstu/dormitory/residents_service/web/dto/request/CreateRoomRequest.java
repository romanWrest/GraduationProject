package ru.dstu.dormitory.residents_service.web.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateRoomRequest(
        @NotBlank @Size(max = 32) String number,
        @NotNull @Min(-5) @Max(100) Short floor,
        @NotNull @Min(1) @Max(20) Short capacity,
        @Size(max = 512) String notes
) {
}
