package ru.dstu.dormitory.requests_service.web.dto;

import java.time.Instant;
import java.util.UUID;

public record CommentDto(
        UUID id,
        UUID requestId,
        UUID authorId,
        String body,
        Instant createdAt
) {
}
