package ru.dstu.dormitory.residents_service.exception;

/**
 * Выбрасывается, когда данные жильца не соответствуют бизнес-правилам вида
 * (например, для STUDENT не заполнены faculty/studyGroup).
 */
public class InvalidResidentDataException extends RuntimeException {
    public InvalidResidentDataException(String message) {
        super(message);
    }
}
