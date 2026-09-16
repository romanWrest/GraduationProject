package ru.dstu.dormitory.consumables_service.exception;

/**
 * Бросается, когда residents-service отвечает 404 на запрос жильца —
 * то есть запись в residents.resident не создана.
 * Маппится в 409 Conflict.
 */
public class ResidentNotEnrolledException extends RuntimeException {
    public ResidentNotEnrolledException(String message) {
        super(message);
    }
}
