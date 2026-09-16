package ru.dstu.dormitory.consumables_service.exception;

public class ConsumableIssueNotFoundException extends RuntimeException {
    public ConsumableIssueNotFoundException(String message) {
        super(message);
    }
}
