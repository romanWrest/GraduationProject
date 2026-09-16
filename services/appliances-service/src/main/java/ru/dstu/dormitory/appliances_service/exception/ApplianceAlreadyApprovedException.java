package ru.dstu.dormitory.appliances_service.exception;

public class ApplianceAlreadyApprovedException extends RuntimeException {
    public ApplianceAlreadyApprovedException(String message) {
        super(message);
    }
}
