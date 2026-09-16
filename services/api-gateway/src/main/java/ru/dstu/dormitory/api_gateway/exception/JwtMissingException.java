package ru.dstu.dormitory.api_gateway.exception;

/**
 * Бросается, когда заголовок Authorization отсутствует или не содержит Bearer-токен.
 */
public class JwtMissingException extends RuntimeException {

    public JwtMissingException(String message) {
        super(message);
    }
}
