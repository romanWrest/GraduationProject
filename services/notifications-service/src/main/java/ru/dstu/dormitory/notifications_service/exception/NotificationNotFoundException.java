package ru.dstu.dormitory.notifications_service.exception;

import java.util.UUID;

public class NotificationNotFoundException extends RuntimeException {

    public NotificationNotFoundException(UUID id) {
        super("Уведомление не найдено: id=%s".formatted(id));
    }
}
