package ru.dstu.dormitory.api_gateway.exception;

/**
 * Бросается, когда клиент превысил лимит частоты запросов.
 */
public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(String message) {
        super(message);
    }
}
