package ru.dstu.dormitory.residents_service.exception;

public class RoomIsFullException extends RuntimeException {
    public RoomIsFullException(String message) {
        super(message);
    }
}
