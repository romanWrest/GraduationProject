package ru.dstu.dormitory.residents_service.exception;

public class ResidentAlreadyExistsException extends RuntimeException {
    public ResidentAlreadyExistsException(String message) {
        super(message);
    }
}
