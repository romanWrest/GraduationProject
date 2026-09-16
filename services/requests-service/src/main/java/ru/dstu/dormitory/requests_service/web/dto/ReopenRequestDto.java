package ru.dstu.dormitory.requests_service.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReopenRequestDto(
        @NotBlank @Size(max = 1000) String reason
) {
}
