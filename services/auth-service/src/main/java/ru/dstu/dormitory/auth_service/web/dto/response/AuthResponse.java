package ru.dstu.dormitory.auth_service.web.dto.response;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresIn
) {
}
