package ru.dstu.dormitory.residents_service.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WriteOffInventoryRequest(
        @NotBlank @Size(max = 512) String reason
) {
}
