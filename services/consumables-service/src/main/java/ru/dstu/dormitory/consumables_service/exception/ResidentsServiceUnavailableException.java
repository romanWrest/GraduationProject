package ru.dstu.dormitory.consumables_service.exception;

public class ResidentsServiceUnavailableException extends RuntimeException {
    public ResidentsServiceUnavailableException(String message) {
        super(message);
    }
}
