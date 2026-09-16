package ru.dstu.dormitory.consumables_service.web.dto.response;

import ru.dstu.dormitory.consumables_service.domain.enums.IssueStatus;
import ru.dstu.dormitory.consumables_service.domain.enums.ReturnCondition;

import java.time.Instant;
import java.util.UUID;

public record ConsumableIssueDto(
        UUID id,
        UUID residentId,
        UUID userId,
        UUID typeId,
        String typeName,
        int quantity,
        IssueStatus status,
        Instant issuedAt,
        UUID issuedBy,
        Instant returnedAt,
        UUID returnedBy,
        ReturnCondition returnCondition,
        String notes
) {
}
