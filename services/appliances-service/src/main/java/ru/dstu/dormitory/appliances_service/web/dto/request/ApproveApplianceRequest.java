package ru.dstu.dormitory.appliances_service.web.dto.request;

import jakarta.validation.constraints.Size;

public record ApproveApplianceRequest(
        @Size(max = 1024) String comment
) {
}
