package ru.dstu.dormitory.auth_service.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequest(
        @NotBlank @Email String email
) {
}
