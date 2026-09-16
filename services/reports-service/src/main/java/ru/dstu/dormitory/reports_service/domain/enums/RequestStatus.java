package ru.dstu.dormitory.reports_service.domain.enums;

/**
 * Статусы заявок (зеркало requests-service).
 */
public enum RequestStatus {
    NEW,
    IN_REVIEW,
    ASSIGNED,
    IN_PROGRESS,
    DONE,
    CLOSED,
    REJECTED,
    CANCELLED
}
