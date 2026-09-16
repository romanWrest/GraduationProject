package ru.dstu.dormitory.auth_service.service;

import ru.dstu.dormitory.auth_service.domain.model.RoleCode;

import java.util.Set;
import java.util.UUID;

public interface JwtService {

    record ParsedAccessToken(UUID userId, String email, Set<RoleCode> roles, String jti) {
    }

    String generateAccessToken(UUID userId, String email, Set<RoleCode> roles);

    ParsedAccessToken parseAndValidate(String token);

    long accessTtlSeconds();
}
