package ru.dstu.dormitory.appliances_service.web.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.dstu.dormitory.appliances_service.domain.enums.ApplianceType;

public record RegisterApplianceRequest(
        @NotNull ApplianceType type,
        @Size(max = 128) String brand,
        @Size(max = 128) String model,
        @Min(1) int powerWatts,
        @Size(max = 512) String photoUrl,
        @Size(max = 512) String notes
) {
}
