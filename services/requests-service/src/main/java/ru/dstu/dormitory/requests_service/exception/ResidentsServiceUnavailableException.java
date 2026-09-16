package ru.dstu.dormitory.requests_service.exception;

public class ResidentsServiceUnavailableException extends RuntimeException {

    public ResidentsServiceUnavailableException(String message) {
        super(message);
    }
}
