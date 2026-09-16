package ru.dstu.dormitory.reports_service.web.dto.response;

import lombok.Builder;

import java.time.LocalDate;
import java.util.UUID;

@Builder
public record ResidentRowDto(
        UUID residentId,
        UUID userId,
        String fullName,
        String kind,
        String faculty,
        String studyGroup,
        String department,
        String roomNumber,
        LocalDate enrolledAt
) {
}
