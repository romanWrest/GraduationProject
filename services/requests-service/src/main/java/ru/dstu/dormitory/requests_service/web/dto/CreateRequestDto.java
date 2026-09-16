package ru.dstu.dormitory.requests_service.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.dstu.dormitory.requests_service.domain.enums.RequestType;

public record CreateRequestDto(
        @NotNull RequestType type,
        @NotBlank @Size(max = 150) String title,
        @NotBlank @Size(max = 10000) String description,
        @Size(max = 1000) String reason
) {
}
