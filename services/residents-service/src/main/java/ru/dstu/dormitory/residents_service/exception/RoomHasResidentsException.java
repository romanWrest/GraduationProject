package ru.dstu.dormitory.residents_service.exception;

public class RoomHasResidentsException extends RuntimeException {
    public RoomHasResidentsException(String message) {
        super(message);
    }
}
