package ru.dstu.dormitory.notifications_service.domain.enums;

/**
 * Типы уведомлений. Привязаны к шаблонам писем 1-к-1.
 */
public enum NotificationType {
    WELCOME,
    ACCOUNT_DEACTIVATED,
    PASSWORD_RESET,
    RESIDENT_ENROLLED,
    RESIDENT_EVICTED,
    RESIDENT_MOVED,
    REQUEST_CREATED,
    REQUEST_ASSIGNED,
    REQUEST_STATUS_CHANGED,
    REQUEST_CLOSED,
    SYSTEM
}
