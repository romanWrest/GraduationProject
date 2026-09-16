package ru.dstu.dormitory.consumables_service.web.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateConsumableTypeRequest(
        @Size(max = 128) String name,
        @Min(0) Integer lowStockThreshold
) {
}
