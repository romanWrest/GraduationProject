package ru.dstu.dormitory.requests_service.exception;

import ru.dstu.dormitory.requests_service.domain.enums.RequestStatus;

public class IllegalStatusTransitionException extends RuntimeException {

    public IllegalStatusTransitionException(RequestStatus from, RequestStatus to) {
        super("Недопустимый переход статуса: %s → %s".formatted(from, to));
    }

    public IllegalStatusTransitionException(String message) {
        super(message);
    }
}
