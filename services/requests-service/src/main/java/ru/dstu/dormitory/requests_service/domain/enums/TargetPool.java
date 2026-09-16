package ru.dstu.dormitory.requests_service.domain.enums;

/**
 * Целевой пул исполнителей (роль-получатель).
 * Значения совпадают с соответствующими {@link RoleCode}, чтобы маршрутизация
 * в /requests/pool могла сопоставлять текущего пользователя с его пулом.
 */
public enum TargetPool {
    EXECUTOR_ELECTRIC,
    EXECUTOR_PLUMBING,
    EXECUTOR_CARPENTRY,
    EXECUTOR_GAS,
    PROPERTY_MANAGER,
    ADMIN
}
