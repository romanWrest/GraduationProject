package ru.dstu.dormitory.requests_service.web.dto;

import jakarta.validation.constraints.Size;

public record CancelRequestDto(
        @Size(max = 1000) String reason
) {
}
