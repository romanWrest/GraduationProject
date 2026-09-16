package ru.dstu.dormitory.appliances_service.exception;

public class ApplianceNotFoundException extends RuntimeException {
    public ApplianceNotFoundException(String message) {
        super(message);
    }
}
