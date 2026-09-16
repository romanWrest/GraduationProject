package ru.dstu.dormitory.appliances_service.exception;

public class IllegalApplianceTransitionException extends RuntimeException {
    public IllegalApplianceTransitionException(String message) {
        super(message);
    }
}
