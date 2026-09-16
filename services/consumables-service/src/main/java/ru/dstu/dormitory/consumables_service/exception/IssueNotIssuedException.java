package ru.dstu.dormitory.consumables_service.exception;

public class IssueNotIssuedException extends RuntimeException {
    public IssueNotIssuedException(String message) {
        super(message);
    }
}
