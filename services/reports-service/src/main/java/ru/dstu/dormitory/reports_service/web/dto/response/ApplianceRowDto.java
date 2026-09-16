package ru.dstu.dormitory.reports_service.web.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ApplianceRowDto(
        UUID applianceId,
        String residentName,
        String roomNumber,
        String type,
        String brand,
        String model,
        Integer powerWatts,
        String status
) {
}
