package ru.dstu.dormitory.reports_service.web.dto.response;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record ConsumableRowDto(
        UUID issueId,
        String residentName,
        String typeName,
        Integer quantity,
        String status,
        Instant issuedAt,
        Instant returnedAt,
        String returnCondition
) {
}
