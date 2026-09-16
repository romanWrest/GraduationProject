package ru.dstu.dormitory.consumables_service.web.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record IssueRequest(
        @NotNull UUID residentId,
        @NotNull UUID typeId,
        @Min(1) int quantity,
        @Size(max = 512) String notes
) {
}
