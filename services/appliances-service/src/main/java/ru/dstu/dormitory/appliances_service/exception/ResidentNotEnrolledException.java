package ru.dstu.dormitory.appliances_service.exception;

/**
 * Бросается, когда у текущего пользователя ещё нет резидентского профиля
 * в residents-service (т.е. он зарегистрирован в auth, но не заселён комендантом).
 * Маппится в 409 Conflict.
 */
public class ResidentNotEnrolledException extends RuntimeException {
    public ResidentNotEnrolledException(String message) {
        super(message);
    }
}
