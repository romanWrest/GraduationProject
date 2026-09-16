package ru.dstu.dormitory.notifications_service.web.advice;

import feign.FeignException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import ru.dstu.dormitory.notifications_service.dto.error.ErrorResponseDTO;
import ru.dstu.dormitory.notifications_service.exception.InvalidNotificationSettingsException;
import ru.dstu.dormitory.notifications_service.exception.MailDeliveryException;
import ru.dstu.dormitory.notifications_service.exception.NotificationAccessDeniedException;
import ru.dstu.dormitory.notifications_service.exception.NotificationNotFoundException;
import ru.dstu.dormitory.notifications_service.exception.TemplateNotFoundException;
import ru.dstu.dormitory.notifications_service.util.LogPatterns;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 404 ──

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(NotificationNotFoundException ex) {
        return setErrorResponseDTO(ex, "Уведомление не найдено", HttpStatus.NOT_FOUND);
    }

    // ── 403 ──

    @ExceptionHandler(NotificationAccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(NotificationAccessDeniedException ex) {
        return setErrorResponseDTO(ex, "Доступ к чужому уведомлению запрещён", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDeniedSpring(AccessDeniedException ex) {
        return setErrorResponseDTO(ex, "Доступ запрещён", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDTO> handleAuthentication(AuthenticationException ex) {
        return setErrorResponseDTO(ex, "Требуется аутентификация", HttpStatus.UNAUTHORIZED);
    }

    // ── 400 ──

    @ExceptionHandler(InvalidNotificationSettingsException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidSettings(InvalidNotificationSettingsException ex) {
        return setErrorResponseDTO(ex, "Некорректные настройки уведомлений", HttpStatus.BAD_REQUEST);
    }

    // ── 500 ──

    @ExceptionHandler(TemplateNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleTemplateNotFound(TemplateNotFoundException ex) {
        return setErrorResponseDTO(ex, "Шаблон письма не найден", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MailDeliveryException.class)
    public ResponseEntity<ErrorResponseDTO> handleMailDelivery(MailDeliveryException ex) {
        return setErrorResponseDTO(ex, "Не удалось отправить письмо", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ── Feign ──

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponseDTO> handleFeign(FeignException ex) {
        int status = ex.status();
        if (status == 404) {
            return setErrorResponseDTO(ex, "Ресурс во внешнем сервисе не найден", HttpStatus.NOT_FOUND);
        }
        return setErrorResponseDTO(ex, "Ошибка при вызове внешнего сервиса", HttpStatus.SERVICE_UNAVAILABLE);
    }

    // ── Validation ──

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
            log.warn(LogPatterns.VALIDATION_ERROR, fe.getField(), fe.getRejectedValue(), fe.getDefaultMessage());
        }
        ErrorResponseDTO body = buildBody(ex, "Ошибка валидации", status);
        body.setValidationErrors(errors);
        log.warn(LogPatterns.CLIENT_ERROR, status.value(), "Ошибка валидации", errors);
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolation(ConstraintViolationException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        Map<String, String> errors = new LinkedHashMap<>();
        for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
            errors.put(v.getPropertyPath().toString(), v.getMessage());
        }
        ErrorResponseDTO body = buildBody(ex, "Нарушение ограничений", status);
        body.setValidationErrors(errors);
        log.warn(LogPatterns.CLIENT_ERROR, status.value(), ex.getMessage(), errors);
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotReadable(HttpMessageNotReadableException ex) {
        return setErrorResponseDTO(ex, LogPatterns.INVALID_JSON_FORMAT, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponseDTO> handleMissingParam(MissingServletRequestParameterException ex) {
        String desc = LogPatterns.MISSING_PARAM.formatted(ex.getParameterName(), ex.getParameterType());
        return setErrorResponseDTO(ex, desc, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponseDTO> handleMissingHeader(MissingRequestHeaderException ex) {
        String desc = LogPatterns.MISSING_HEADER.formatted(ex.getHeaderName());
        return setErrorResponseDTO(ex, desc, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String expected = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "неизвестно";
        String desc = LogPatterns.TYPE_MISMATCH.formatted(ex.getName(), expected);
        return setErrorResponseDTO(ex, desc, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        String[] supported = ex.getSupportedMethods();
        String supportedList = supported == null ? LogPatterns.SUPPORTED_METHODS_UNDEFINED : String.join(", ", supported);
        String desc = LogPatterns.METHOD_NOT_SUPPORTED.formatted(ex.getMethod(), supportedList);
        return setErrorResponseDTO(ex, desc, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNoHandler(NoHandlerFoundException ex) {
        String desc = LogPatterns.PATH_NOT_FOUND.formatted(ex.getRequestURL());
        return setErrorResponseDTO(ex, desc, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgument(IllegalArgumentException ex) {
        return setErrorResponseDTO(ex, "Некорректный аргумент", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleAny(Exception ex) {
        log.error(LogPatterns.UNHANDLED_EXCEPTION, ex);
        return setErrorResponseDTO(ex, "Внутренняя ошибка сервиса", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponseDTO> setErrorResponseDTO(Exception ex,
                                                                 String description,
                                                                 HttpStatus status) {
        ErrorResponseDTO body = buildBody(ex, description, status);
        if (status.is4xxClientError()) {
            log.warn(LogPatterns.CLIENT_ERROR, status.value(), ex.getMessage(), description);
        } else {
            log.error(LogPatterns.SERVER_ERROR, status.value(), ex.getMessage(), description);
        }
        return new ResponseEntity<>(body, status);
    }

    private ErrorResponseDTO buildBody(Exception ex, String description, HttpStatus status) {
        return ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .description(description)
                .build();
    }
}
