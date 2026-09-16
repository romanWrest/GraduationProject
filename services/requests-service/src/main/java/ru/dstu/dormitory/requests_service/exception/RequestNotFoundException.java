package ru.dstu.dormitory.requests_service.exception;

import java.util.UUID;

public class RequestNotFoundException extends RuntimeException {

    public RequestNotFoundException(UUID id) {
        super("Заявка не найдена: %s".formatted(id));
    }
}
