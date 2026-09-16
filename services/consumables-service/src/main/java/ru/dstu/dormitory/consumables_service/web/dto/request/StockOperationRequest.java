package ru.dstu.dormitory.consumables_service.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StockOperationRequest(
        @NotNull Integer delta,
        @NotBlank @Size(max = 255) String reason
) {
}
