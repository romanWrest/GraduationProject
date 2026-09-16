package ru.dstu.dormitory.consumables_service.web.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.dstu.dormitory.consumables_service.domain.enums.ConsumableUnit;

public record CreateConsumableTypeRequest(
        @NotBlank @Size(max = 128) String name,
        @NotNull ConsumableUnit unit,
        @Min(0) int stock,
        @Min(0) Integer lowStockThreshold
) {
}
