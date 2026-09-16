package ru.dstu.dormitory.api_gateway.exception;

/**
 * Бросается, когда срок действия JWT истёк.
 */
public class JwtExpiredException extends RuntimeException {

    public JwtExpiredException(String message) {
        super(message);
    }

    public JwtExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
