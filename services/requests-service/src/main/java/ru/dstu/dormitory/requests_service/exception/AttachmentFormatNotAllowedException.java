package ru.dstu.dormitory.requests_service.exception;

public class AttachmentFormatNotAllowedException extends RuntimeException {

    public AttachmentFormatNotAllowedException(String contentType) {
        super("Формат файла '%s' не разрешён".formatted(contentType));
    }
}
