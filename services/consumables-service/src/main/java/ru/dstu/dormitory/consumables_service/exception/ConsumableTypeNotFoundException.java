package ru.dstu.dormitory.consumables_service.exception;

public class ConsumableTypeNotFoundException extends RuntimeException {
    public ConsumableTypeNotFoundException(String message) {
        super(message);
    }
}
