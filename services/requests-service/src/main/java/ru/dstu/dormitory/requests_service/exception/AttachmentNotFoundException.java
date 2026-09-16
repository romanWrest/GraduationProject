package ru.dstu.dormitory.requests_service.exception;

import java.util.UUID;

public class AttachmentNotFoundException extends RuntimeException {

    public AttachmentNotFoundException(UUID id) {
        super("Вложение не найдено: %s".formatted(id));
    }
}
