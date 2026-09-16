package ru.dstu.dormitory.residents_service.web.advice;

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
import org.springframework.web.servlet.resource.NoResourceFoundException;
import ru.dstu.dormitory.residents_service.dto.error.ErrorResponseDTO;
import ru.dstu.dormitory.residents_service.exception.AuthServiceUnavailableException;
import ru.dstu.dormitory.residents_service.exception.ForbiddenActionException;
import ru.dstu.dormitory.residents_service.exception.InvalidEvictionDateException;
import ru.dstu.dormitory.residents_service.exception.InvalidResidentDataException;
import ru.dstu.dormitory.residents_service.exception.InventoryItemNotFoundException;
import ru.dstu.dormitory.residents_service.exception.ResidentAlreadyExistsException;
import ru.dstu.dormitory.residents_service.exception.ResidentNotFoundException;
import ru.dstu.dormitory.residents_service.exception.ResidentNotInRoomException;
import ru.dstu.dormitory.residents_service.exception.RoomHasResidentsException;
import ru.dstu.dormitory.residents_service.exception.RoomIsFullException;
import ru.dstu.dormitory.residents_service.exception.RoomNotFoundException;
import ru.dstu.dormitory.residents_service.util.LogPatterns;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 404 ──

    @ExceptionHandler(ResidentNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResidentNotFound(ResidentNotFoundException ex) {
        return setErrorResponseDTO(ex, "Жилец не найден", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleRoomNotFound(RoomNotFoundException ex) {
        return setErrorResponseDTO(ex, "Комната не найдена", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InventoryItemNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleInventoryNotFound(InventoryItemNotFoundException ex) {
        return setErrorResponseDTO(ex, "Позиция инвентаря не найдена", HttpStatus.NOT_FOUND);
    }

    // ── 409 ──

    @ExceptionHandler(ResidentAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleResidentExists(ResidentAlreadyExistsException ex) {
        return setErrorResponseDTO(ex, "Жилец уже существует", HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RoomIsFullException.class)
    public ResponseEntity<ErrorResponseDTO> handleRoomFull(RoomIsFullException ex) {
        return setErrorResponseDTO(ex, "В комнате нет свободных мест", HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RoomHasResidentsException.class)
    public ResponseEntity<ErrorResponseDTO> handleRoomHasResidents(RoomHasResidentsException ex) {
        return setErrorResponseDTO(ex, "В комнате есть жильцы", HttpStatus.CONFLICT);
    }

    // ── 400 ──

    @ExceptionHandler(ResidentNotInRoomException.class)
    public ResponseEntity<ErrorResponseDTO> handleResidentNotInRoom(ResidentNotInRoomException ex) {
        return setErrorResponseDTO(ex, "Жилец не находится в указанной комнате", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidEvictionDateException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidEvictionDate(InvalidEvictionDateException ex) {
        return setErrorResponseDTO(ex, "Некорректная дата выселения", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidResidentDataException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidResidentData(InvalidResidentDataException ex) {
        return setErrorResponseDTO(ex, "Некорректные данные жильца", HttpStatus.BAD_REQUEST);
    }

    // ── 403 ──

    @ExceptionHandler(ForbiddenActionException.class)
    public ResponseEntity<ErrorResponseDTO> handleForbidden(ForbiddenActionException ex) {
        return setErrorResponseDTO(ex, "Действие запрещено", HttpStatus.FORBIDDEN);
    }

    // ── 503 ──

    @ExceptionHandler(AuthServiceUnavailableException.class)
    public ResponseEntity<ErrorResponseDTO> handleAuthUnavailable(AuthServiceUnavailableException ex) {
        return setErrorResponseDTO(ex, "auth-service недоступен", HttpStatus.SERVICE_UNAVAILABLE);
    }

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

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNoResource(NoResourceFoundException ex) {
        String desc = LogPatterns.PATH_NOT_FOUND.formatted(ex.getResourcePath());
        return setErrorResponseDTO(ex, desc, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(AccessDeniedException ex) {
        return setErrorResponseDTO(ex, "Доступ запрещён", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDTO> handleAuthentication(AuthenticationException ex) {
        return setErrorResponseDTO(ex, "Требуется аутентификация", HttpStatus.UNAUTHORIZED);
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
