package ru.dstu.dormitory.requests_service.web.dto;

import java.time.Instant;
import java.util.UUID;

public record AttachmentDto(
        UUID id,
        UUID requestId,
        String originalName,
        long sizeBytes,
        String contentType,
        UUID uploadedBy,
        Instant uploadedAt
) {
}
