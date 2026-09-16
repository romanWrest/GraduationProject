package ru.dstu.dormitory.residents_service.exception;

public class InvalidEvictionDateException extends RuntimeException {
    public InvalidEvictionDateException(String message) {
        super(message);
    }
}
