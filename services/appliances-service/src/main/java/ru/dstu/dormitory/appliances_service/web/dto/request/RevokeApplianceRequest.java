package ru.dstu.dormitory.appliances_service.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RevokeApplianceRequest(
        @NotBlank @Size(max = 1024) String reason
) {
}
