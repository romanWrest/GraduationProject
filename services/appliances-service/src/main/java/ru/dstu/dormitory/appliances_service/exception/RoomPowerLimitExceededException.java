package ru.dstu.dormitory.appliances_service.exception;

public class RoomPowerLimitExceededException extends RuntimeException {
    public RoomPowerLimitExceededException(String message) {
        super(message);
    }
}
