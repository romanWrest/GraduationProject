package ru.dstu.dormitory.requests_service.exception;

public class RequestCannotBeReopenedException extends RuntimeException {

    public RequestCannotBeReopenedException(String message) {
        super(message);
    }
}
