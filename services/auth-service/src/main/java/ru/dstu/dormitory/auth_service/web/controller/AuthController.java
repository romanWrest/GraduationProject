package ru.dstu.dormitory.auth_service.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.dstu.dormitory.auth_service.mapper.UserMapper;
import ru.dstu.dormitory.auth_service.security.CurrentUser;
import ru.dstu.dormitory.auth_service.security.UserPrincipal;
import ru.dstu.dormitory.auth_service.service.AuthService;
import ru.dstu.dormitory.auth_service.service.PasswordResetService;
import ru.dstu.dormitory.auth_service.service.UserService;
import ru.dstu.dormitory.auth_service.web.dto.response.UserDto;
import ru.dstu.dormitory.auth_service.web.dto.request.LoginRequest;
import ru.dstu.dormitory.auth_service.web.dto.request.PasswordChangeRequest;
import ru.dstu.dormitory.auth_service.web.dto.request.PasswordResetConfirmRequest;
import ru.dstu.dormitory.auth_service.web.dto.request.PasswordResetRequest;
import ru.dstu.dormitory.auth_service.web.dto.request.RefreshRequest;
import ru.dstu.dormitory.auth_service.web.dto.response.AuthResponse;

import java.util.UUID;

@Tag(name = "Auth", description = "Аутентификация и управление сессиями")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final UserService userService;
    private final UserMapper userMapper;

    @Operation(summary = "Профиль текущего пользователя (алиас /api/v1/users/me)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me")
    public ResponseEntity<UserDto> me(@CurrentUser UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userMapper.toDto(userService.getById(principal.userId())));
    }

    @Operation(summary = "Логин по email/паролю")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                              HttpServletRequest http) {
        return ResponseEntity.ok(authService.login(request, resolveClientIp(http)));
    }

    @Operation(summary = "Обновление access/refresh пары")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken()));
    }

    @Operation(summary = "Логаут (отзыв refresh токена)", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request,
                                       @CurrentUser UserPrincipal principal) {
        UUID userId = principal == null ? null : principal.userId();
        authService.logout(request.refreshToken(), userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Запрос письма для сброса пароля")
    @PostMapping("/password-reset/request")
    public ResponseEntity<Void> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        passwordResetService.requestReset(request.email());
        return ResponseEntity.accepted().build();
    }

    @Operation(summary = "Применение сброса пароля по токену")
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<Void> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        passwordResetService.applyReset(request.token(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Смена собственного пароля", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/password-change")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request,
                                               @CurrentUser UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        userService.changePassword(principal.userId(), request.oldPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int comma = forwarded.indexOf(',');
            return comma < 0 ? forwarded.trim() : forwarded.substring(0, comma).trim();
        }
        return request.getRemoteAddr();
    }
}
