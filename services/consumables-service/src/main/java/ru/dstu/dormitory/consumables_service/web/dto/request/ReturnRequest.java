package ru.dstu.dormitory.consumables_service.web.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.dstu.dormitory.consumables_service.domain.enums.ReturnCondition;

public record ReturnRequest(
        @NotNull ReturnCondition condition,
        @Size(max = 512) String notes
) {
}
