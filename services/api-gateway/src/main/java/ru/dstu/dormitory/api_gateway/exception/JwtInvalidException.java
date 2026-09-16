package ru.dstu.dormitory.api_gateway.exception;

/**
 * Бросается, когда JWT не проходит валидацию подписи, формата или содержит некорректные claims.
 */
public class JwtInvalidException extends RuntimeException {

    public JwtInvalidException(String message) {
        super(message);
    }

    public JwtInvalidException(String message, Throwable cause) {
        super(message, cause);
    }
}
