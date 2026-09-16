package ru.dstu.dormitory.api_gateway.advice;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ConnectTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.dstu.dormitory.api_gateway.dto.error.ErrorResponseDTO;
import ru.dstu.dormitory.api_gateway.exception.DownstreamUnavailableException;
import ru.dstu.dormitory.api_gateway.exception.JwtExpiredException;
import ru.dstu.dormitory.api_gateway.exception.JwtInvalidException;
import ru.dstu.dormitory.api_gateway.exception.JwtMissingException;
import ru.dstu.dormitory.api_gateway.exception.RateLimitExceededException;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.concurrent.TimeoutException;

import static ru.dstu.dormitory.api_gateway.util.LogPatterns.CLIENT_ERROR;
import static ru.dstu.dormitory.api_gateway.util.LogPatterns.DOWNSTREAM_ERROR;
import static ru.dstu.dormitory.api_gateway.util.LogPatterns.DOWNSTREAM_TIMEOUT;
import static ru.dstu.dormitory.api_gateway.util.LogPatterns.SERVER_ERROR;
import static ru.dstu.dormitory.api_gateway.util.LogPatterns.UNHANDLED_EXCEPTION;

/**
 * Реактивный глобальный обработчик ошибок. Возвращает {@link ErrorResponseDTO}
 * в едином формате. Регистрируется с приоритетом выше DefaultErrorWebExceptionHandler.
 */
@Slf4j
@Component
@Order(-2)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        Resolved resolved = resolve(ex, exchange);

        ErrorResponseDTO body = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .error(resolved.status.getReasonPhrase())
                .message(resolved.message)
                .description(resolved.description)
                .path(exchange.getRequest().getURI().getPath())
                .build();

        exchange.getResponse().setStatusCode(resolved.status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        if (resolved.status == HttpStatus.TOO_MANY_REQUESTS
                && !exchange.getResponse().getHeaders().containsKey(HttpHeaders.RETRY_AFTER)) {
            exchange.getResponse().getHeaders().set(HttpHeaders.RETRY_AFTER, "1");
        }

        DataBufferFactory factory = exchange.getResponse().bufferFactory();
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(body);
            DataBuffer buffer = factory.wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (IOException serializationError) {
            log.error("Не удалось сериализовать тело ошибки", serializationError);
            byte[] fallback = ("{\"error\":\"" + resolved.status.getReasonPhrase()
                    + "\",\"message\":\"" + safe(resolved.message) + "\"}")
                    .getBytes(StandardCharsets.UTF_8);
            return exchange.getResponse().writeWith(Mono.just(factory.wrap(fallback)));
        }
    }

    private Resolved resolve(Throwable ex, ServerWebExchange exchange) {
        String path = exchange.getRequest().getURI().getPath();

        if (ex instanceof JwtMissingException e) {
            log.warn(CLIENT_ERROR, 401, "JWT_MISSING", e.getMessage());
            return new Resolved(HttpStatus.UNAUTHORIZED, e.getMessage(), "Требуется авторизация");
        }
        if (ex instanceof JwtExpiredException e) {
            log.warn(CLIENT_ERROR, 401, "JWT_EXPIRED", e.getMessage());
            return new Resolved(HttpStatus.UNAUTHORIZED, "Срок действия токена истёк",
                    "Получите новый access-токен через /api/v1/auth/refresh");
        }
        if (ex instanceof JwtInvalidException e) {
            log.warn(CLIENT_ERROR, 401, "JWT_INVALID", e.getMessage());
            return new Resolved(HttpStatus.UNAUTHORIZED, "Невалидный токен", e.getMessage());
        }
        if (ex instanceof RateLimitExceededException e) {
            log.warn(CLIENT_ERROR, 429, "RATE_LIMIT", e.getMessage());
            return new Resolved(HttpStatus.TOO_MANY_REQUESTS,
                    "Слишком много запросов",
                    "Повторите запрос позже");
        }
        if (ex instanceof DownstreamUnavailableException e) {
            log.error(DOWNSTREAM_ERROR, "downstream", 503, path);
            return new Resolved(HttpStatus.SERVICE_UNAVAILABLE,
                    "Сервис временно недоступен", e.getMessage());
        }
        if (isDownstreamConnectivity(ex)) {
            log.error(DOWNSTREAM_ERROR, "downstream", 503, path);
            return new Resolved(HttpStatus.SERVICE_UNAVAILABLE,
                    "Сервис временно недоступен",
                    "Не удалось подключиться к downstream-сервису");
        }
        if (isTimeout(ex)) {
            log.error(DOWNSTREAM_TIMEOUT, "downstream", path);
            return new Resolved(HttpStatus.GATEWAY_TIMEOUT,
                    "Превышено время ожидания ответа",
                    "Downstream-сервис не ответил в отведённое время");
        }
        if (ex instanceof ResponseStatusException rse) {
            HttpStatus status = HttpStatus.resolve(rse.getStatusCode().value());
            if (status == null) {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
            String marker = status.is4xxClientError() ? CLIENT_ERROR : SERVER_ERROR;
            log.warn(marker, status.value(), status.getReasonPhrase(),
                    rse.getReason() == null ? "-" : rse.getReason());
            return new Resolved(status,
                    status.getReasonPhrase(),
                    rse.getReason());
        }
        if (ex instanceof NoResourceFoundException) {
            log.warn(CLIENT_ERROR, 404, "NOT_FOUND", path);
            return new Resolved(HttpStatus.NOT_FOUND,
                    "Маршрут не найден",
                    "Путь %s не зарегистрирован".formatted(path));
        }

        log.error(UNHANDLED_EXCEPTION, ex);
        return new Resolved(HttpStatus.INTERNAL_SERVER_ERROR,
                "Внутренняя ошибка сервера",
                ex.getClass().getSimpleName());
    }

    private boolean isDownstreamConnectivity(Throwable ex) {
        Throwable cur = ex;
        while (cur != null) {
            if (cur instanceof ConnectException
                    || cur instanceof WebClientRequestException
                    || cur instanceof ConnectTimeoutException) {
                return true;
            }
            cur = cur.getCause();
        }
        return false;
    }

    private boolean isTimeout(Throwable ex) {
        Throwable cur = ex;
        while (cur != null) {
            if (cur instanceof TimeoutException) {
                return true;
            }
            cur = cur.getCause();
        }
        return false;
    }

    private String safe(String s) {
        return s == null ? "" : s.replace("\"", "'");
    }

    private record Resolved(HttpStatus status, String message, String description) {
    }
}
