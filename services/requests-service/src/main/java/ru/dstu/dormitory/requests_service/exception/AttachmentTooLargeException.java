package ru.dstu.dormitory.requests_service.exception;

public class AttachmentTooLargeException extends RuntimeException {

    public AttachmentTooLargeException(long actualSize, long maxSize) {
        super("Размер файла %d байт превышает допустимый лимит %d байт".formatted(actualSize, maxSize));
    }
}
