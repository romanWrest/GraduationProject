package ru.dstu.dormitory.requests_service.domain.enums;

/**
 * Роли пользователей. Значения совпадают с auth-service.
 */
public enum RoleCode {
    ADMIN,
    RESIDENT,
    GATEKEEPER,
    EXECUTOR_ELECTRIC,
    EXECUTOR_PLUMBING,
    EXECUTOR_CARPENTRY,
    EXECUTOR_GAS,
    PROPERTY_MANAGER
}
