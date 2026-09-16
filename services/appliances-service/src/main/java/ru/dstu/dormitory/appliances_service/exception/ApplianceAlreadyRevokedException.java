package ru.dstu.dormitory.appliances_service.exception;

public class ApplianceAlreadyRevokedException extends RuntimeException {
    public ApplianceAlreadyRevokedException(String message) {
        super(message);
    }
}
