package ru.dstu.dormitory.requests_service.exception;

public class RequestAlreadyClosedException extends RuntimeException {

    public RequestAlreadyClosedException() {
        super("Заявка уже закрыта");
    }
}
