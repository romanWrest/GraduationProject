package ru.dstu.dormitory.requests_service.domain.enums;

/**
 * Типы заявок. Определяют целевой пул (роль-исполнителя) при маршрутизации.
 */
public enum RequestType {
    REPAIR_ELECTRIC,
    REPAIR_PLUMBING,
    REPAIR_CARPENTRY,
    REPAIR_GAS,
    LINEN_REPLACEMENT,
    GUEST_PASS,
    ITEM_MOVEMENT,
    RELOCATION,
    COMPLAINT,
    ELECTRICAL_APPLIANCE,
    OTHER
}
