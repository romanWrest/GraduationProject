package ru.dstu.dormitory.requests_service.exception;

public class RequestCannotBeCancelledException extends RuntimeException {

    public RequestCannotBeCancelledException(String message) {
        super(message);
    }
}
