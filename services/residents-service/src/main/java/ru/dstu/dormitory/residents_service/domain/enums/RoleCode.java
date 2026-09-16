package ru.dstu.dormitory.residents_service.domain.enums;

/**
 * Роли пользователей. Значения совпадают с auth-service, т.к. ролевые коды
 * присутствуют в JWT, который выдаёт auth-service.
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
