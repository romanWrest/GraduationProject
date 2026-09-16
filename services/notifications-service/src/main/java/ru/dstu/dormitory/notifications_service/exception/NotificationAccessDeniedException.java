package ru.dstu.dormitory.notifications_service.exception;

public class NotificationAccessDeniedException extends RuntimeException {

    public NotificationAccessDeniedException() {
        super("Уведомление принадлежит другому пользователю");
    }
}
