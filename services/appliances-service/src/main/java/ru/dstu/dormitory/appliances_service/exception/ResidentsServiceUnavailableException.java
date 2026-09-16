package ru.dstu.dormitory.appliances_service.exception;

public class ResidentsServiceUnavailableException extends RuntimeException {
    public ResidentsServiceUnavailableException(String message) {
        super(message);
    }
}
