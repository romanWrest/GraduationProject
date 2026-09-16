package ru.dstu.dormitory.residents_service.web.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.dstu.dormitory.residents_service.domain.enums.InventoryState;
import ru.dstu.dormitory.residents_service.domain.enums.InventoryType;

public record CreateInventoryRequest(
        @NotNull InventoryType type,
        @Size(max = 128) String serialNumber,
        @NotNull InventoryState state,
        @Size(max = 512) String notes
) {
}
