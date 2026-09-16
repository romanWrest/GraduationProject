package ru.dstu.dormitory.notifications_service.exception;

public class InvalidNotificationSettingsException extends RuntimeException {

    public InvalidNotificationSettingsException(String message) {
        super(message);
    }
}
