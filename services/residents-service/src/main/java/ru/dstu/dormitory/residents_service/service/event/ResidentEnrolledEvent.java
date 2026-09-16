package ru.dstu.dormitory.residents_service.service.event;

import ru.dstu.dormitory.residents_service.domain.enums.ResidentKind;

import java.time.LocalDate;
import java.util.UUID;

public record ResidentEnrolledEvent(
        UUID residentId,
        UUID userId,
        ResidentKind kind,
        UUID roomId,
        LocalDate enrolledAt
) {
}
