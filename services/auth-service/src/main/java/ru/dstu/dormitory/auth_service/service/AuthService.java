package ru.dstu.dormitory.auth_service.service;

import ru.dstu.dormitory.auth_service.web.dto.request.LoginRequest;
import ru.dstu.dormitory.auth_service.web.dto.response.AuthResponse;

import java.util.UUID;

public interface AuthService {

    AuthResponse login(LoginRequest request, String clientIp);

    AuthResponse refresh(String rawRefreshToken);

    void logout(String rawRefreshToken, UUID userId);
}
