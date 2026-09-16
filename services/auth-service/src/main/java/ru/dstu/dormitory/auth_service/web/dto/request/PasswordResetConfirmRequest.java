package ru.dstu.dormitory.auth_service.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetConfirmRequest(
        @NotBlank String token,
        @NotBlank String newPassword
) {
}
