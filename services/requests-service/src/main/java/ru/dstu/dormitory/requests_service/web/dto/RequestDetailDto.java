package ru.dstu.dormitory.requests_service.web.dto;

import java.util.List;

public record RequestDetailDto(
        RequestDto request,
        List<CommentDto> comments,
        List<AttachmentDto> attachments
) {
}
