package ru.dstu.dormitory.auth_service.web.dto.request;

import jakarta.validation.constraints.Email;

public record UpdateUserRequest(
        String fullName,
        String phone,
        @Email String email
) {
}
