package ru.dstu.dormitory.residents_service.exception;

public class ResidentNotInRoomException extends RuntimeException {
    public ResidentNotInRoomException(String message) {
        super(message);
    }
}
