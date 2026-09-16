package ru.dstu.dormitory.residents_service.exception;

public class ResidentNotFoundException extends RuntimeException {
    public ResidentNotFoundException(String message) {
        super(message);
    }
}
