package ru.dstu.dormitory.residents_service.web.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.dstu.dormitory.residents_service.domain.enums.ResidentKind;

import java.time.LocalDate;
import java.util.UUID;

public record EnrollResidentRequest(
        @NotNull UUID userId,
        @NotNull ResidentKind kind,
        @Size(max = 128) String faculty,
        @Size(max = 64) String studyGroup,
        @Size(max = 128) String department,
        @Size(max = 32) String phone,
        @Size(max = 512) String contactInfo,
        @NotNull UUID roomId,
        @NotNull LocalDate enrolledAt
) {
}
