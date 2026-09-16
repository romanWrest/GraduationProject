package ru.dstu.dormitory.requests_service.exception;

public class AttachmentCannotBeDeletedException extends RuntimeException {

    public AttachmentCannotBeDeletedException() {
        super("Вложение нельзя удалить: заявка уже назначена исполнителю");
    }
}
