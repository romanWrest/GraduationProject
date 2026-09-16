package ru.dstu.dormitory.auth_service.web.advice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
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
import ru.dstu.dormitory.auth_service.web.dto.error.ErrorResponseDTO;
import ru.dstu.dormitory.auth_service.exception.ForbiddenActionException;
import ru.dstu.dormitory.auth_service.exception.InvalidCredentialsException;
import ru.dstu.dormitory.auth_service.exception.PasswordResetTokenInvalidException;
import ru.dstu.dormitory.auth_service.exception.RateLimitExceededException;
import ru.dstu.dormitory.auth_service.exception.RefreshTokenInvalidException;
import ru.dstu.dormitory.auth_service.exception.RefreshTokenReuseException;
import ru.dstu.dormitory.auth_service.exception.UserAlreadyExistsException;
import ru.dstu.dormitory.auth_service.exception.UserNotFoundException;
import ru.dstu.dormitory.auth_service.exception.WeakPasswordException;
import ru.dstu.dormitory.auth_service.util.LogPatterns;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidCredentials(InvalidCredentialsException ex) {
        return setErrorResponseDTO(ex, "Invalid email or password", HttpStatus.UNAUTHORIZED);
    }
    List<Integer> list;
    @ExceptionHandler(RefreshTokenInvalidException.class)
    public ResponseEntity<ErrorResponseDTO> handleRefreshTokenInvalid(RefreshTokenInvalidException ex) {
        return setErrorResponseDTO(ex, "Refresh token is invalid or expired", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(RefreshTokenReuseException.class)
    public ResponseEntity<ErrorResponseDTO> handleRefreshReuse(RefreshTokenReuseException ex) {
        return setErrorResponseDTO(ex, "Refresh token reuse detected, family invalidated", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserNotFound(UserNotFoundException ex) {
        return setErrorResponseDTO(ex, "User not found", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return setErrorResponseDTO(ex, "User already exists", HttpStatus.CONFLICT);
    }

    @ExceptionHandler(PasswordResetTokenInvalidException.class)
    public ResponseEntity<ErrorResponseDTO> handleResetTokenInvalid(PasswordResetTokenInvalidException ex) {
        return setErrorResponseDTO(ex, "Password reset token is invalid or expired", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(WeakPasswordException.class)
    public ResponseEntity<ErrorResponseDTO> handleWeakPassword(WeakPasswordException ex) {
        return setErrorResponseDTO(ex, "Password does not meet policy", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ForbiddenActionException.class)
    public ResponseEntity<ErrorResponseDTO> handleForbidden(ForbiddenActionException ex) {
        return setErrorResponseDTO(ex, "Action is forbidden", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponseDTO> handleRateLimit(RateLimitExceededException ex) {
        HttpStatus status = HttpStatus.TOO_MANY_REQUESTS;
        ErrorResponseDTO body = buildBody(ex, "Rate limit exceeded", status);
        log.warn(LogPatterns.CLIENT_ERROR, status.value(), ex.getMessage(), "Rate limit exceeded");
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.RETRY_AFTER, String.valueOf(ex.getRetryAfterSeconds()));
        return new ResponseEntity<>(body, headers, status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
            //TODO
            log.warn(LogPatterns.VALIDATION_ERROR, fe.getField(), fe.getRejectedValue(), fe.getDefaultMessage());
        }
        ErrorResponseDTO body = buildBody(ex, "Validation failed", status);
        body.setValidationErrors(errors);
        log.warn(LogPatterns.CLIENT_ERROR, status.value(), "Validation failed", errors);
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolation(ConstraintViolationException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        Map<String, String> errors = new LinkedHashMap<>();
        for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
            errors.put(v.getPropertyPath().toString(), v.getMessage());
        }
        ErrorResponseDTO body = buildBody(ex, "Constraint violation", status);
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
        return setErrorResponseDTO(ex, "Access denied", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDTO> handleAuthentication(AuthenticationException ex) {
        return setErrorResponseDTO(ex, "Authentication required", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgument(IllegalArgumentException ex) {
        return setErrorResponseDTO(ex, "Illegal argument", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleAny(Exception ex) {
        log.error(LogPatterns.UNHANDLED_EXCEPTION, ex);
        return setErrorResponseDTO(ex, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
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
