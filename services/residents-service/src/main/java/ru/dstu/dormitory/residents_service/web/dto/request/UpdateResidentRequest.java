package ru.dstu.dormitory.residents_service.web.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateResidentRequest(
        @Size(max = 128) String faculty,
        @Size(max = 64) String studyGroup,
        @Size(max = 128) String department,
        @Size(max = 32) String phone,
        @Size(max = 512) String contactInfo
) {
}
