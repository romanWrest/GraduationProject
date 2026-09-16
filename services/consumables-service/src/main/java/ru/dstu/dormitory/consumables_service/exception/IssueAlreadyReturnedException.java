package ru.dstu.dormitory.consumables_service.exception;

public class IssueAlreadyReturnedException extends RuntimeException {
    public IssueAlreadyReturnedException(String message) {
        super(message);
    }
}
