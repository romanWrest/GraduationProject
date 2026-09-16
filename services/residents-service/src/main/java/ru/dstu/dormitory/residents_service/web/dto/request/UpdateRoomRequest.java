package ru.dstu.dormitory.residents_service.web.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateRoomRequest(
        @Size(max = 32) String number,
        @Min(-5) @Max(100) Short floor,
        @Min(1) @Max(20) Short capacity,
        @Size(max = 512) String notes
) {
}
