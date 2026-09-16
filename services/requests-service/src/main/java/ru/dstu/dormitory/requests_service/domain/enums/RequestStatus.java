package ru.dstu.dormitory.requests_service.domain.enums;

/**
 * Статусы жизненного цикла заявки.
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
