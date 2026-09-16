package ru.dstu.dormitory.requests_service.exception;

public class StorageUnavailableException extends RuntimeException {

    public StorageUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public StorageUnavailableException(String message) {
        super(message);
    }
}
