package ru.dstu.dormitory.api_gateway.exception;

/**
 * Бросается, когда downstream-сервис недоступен (connection refused, timeout, 5xx).
 */
public class DownstreamUnavailableException extends RuntimeException {

    public DownstreamUnavailableException(String message) {
        super(message);
    }

    public DownstreamUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
