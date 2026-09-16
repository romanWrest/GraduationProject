package ru.dstu.dormitory.requests_service.web.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record PatchActionDto(
        @NotNull PatchAction action,
        UUID assigneeId,
        Instant scheduledAt,
        String resolutionComment,
        String rejectionReason
) {

    public enum PatchAction {
        ASSIGN,
        START,
        DONE,
        CONFIRM,
        REJECT,
        REVIEW
    }
}
