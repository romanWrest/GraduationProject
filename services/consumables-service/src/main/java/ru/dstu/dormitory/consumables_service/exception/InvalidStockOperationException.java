package ru.dstu.dormitory.consumables_service.exception;

public class InvalidStockOperationException extends RuntimeException {
    public InvalidStockOperationException(String message) {
        super(message);
    }
}
