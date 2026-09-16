package ru.dstu.dormitory.residents_service.web.dto.request;

import jakarta.validation.constraints.Size;
import ru.dstu.dormitory.residents_service.domain.enums.InventoryState;

public record UpdateInventoryRequest(
        InventoryState state,
        @Size(max = 128) String serialNumber,
        @Size(max = 512) String notes
) {
}
