package ru.dstu.dormitory.auth_service.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.auth_service.aspect.annotation.LogMethod;
import ru.dstu.dormitory.auth_service.domain.model.Role;
import ru.dstu.dormitory.auth_service.domain.model.RoleCode;
import ru.dstu.dormitory.auth_service.domain.model.User;
import ru.dstu.dormitory.auth_service.domain.repo.UserRepository;
import ru.dstu.dormitory.auth_service.exception.InvalidCredentialsException;
import ru.dstu.dormitory.auth_service.service.AuditService;
import ru.dstu.dormitory.auth_service.service.AuthService;
import ru.dstu.dormitory.auth_service.service.JwtService;
import ru.dstu.dormitory.auth_service.service.RateLimiterService;
import ru.dstu.dormitory.auth_service.service.RefreshTokenService;
import ru.dstu.dormitory.auth_service.util.LogPatterns;
import ru.dstu.dormitory.auth_service.web.dto.request.LoginRequest;
import ru.dstu.dormitory.auth_service.web.dto.response.AuthResponse;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String TARGET_USER = "User";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RateLimiterService rateLimiterService;
    private final AuditService auditService;

    @Override
    @Transactional
    @LogMethod(value = "Логин пользователя", logArgs = {"request", "clientIp"}, maskArgs = {"request"})
    public AuthResponse login(LoginRequest request, String clientIp) {
        rateLimiterService.checkLogin(clientIp, request.email());

        User user = userRepository.findByEmailIgnoreCase(request.email()).orElse(null);
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn(LogPatterns.LOGIN_FAILED, request.email());
            auditService.record(user == null ? null : user.getId(),
                    AuditService.Action.LOGIN_FAILED, TARGET_USER,
                    user == null ? request.email() : user.getId().toString(),
                    Map.of("ip", nullSafe(clientIp)));
            throw new InvalidCredentialsException("Неверный email или пароль");
        }
        if (!Boolean.TRUE.equals(user.getActive())) {
            auditService.record(user.getId(), AuditService.Action.LOGIN_FAILED, TARGET_USER,
                    user.getId().toString(), Map.of("ip", nullSafe(clientIp), "reason", "inactive"));
            throw new InvalidCredentialsException("Учётная запись деактивирована");
        }

        Set<RoleCode> roles = user.getRoles().stream().map(Role::getCode).collect(Collectors.toUnmodifiableSet());
        String access = jwtService.generateAccessToken(user.getId(), user.getEmail(), roles);
        RefreshTokenService.IssuedToken refresh = refreshTokenService.issueNew(user.getId());

        auditService.record(user.getId(), AuditService.Action.LOGIN_SUCCESS, TARGET_USER,
                user.getId().toString(), Map.of("ip", nullSafe(clientIp)));
        log.info(LogPatterns.LOGIN_SUCCESS, user.getEmail());
        return new AuthResponse(access, refresh.rawToken(), jwtService.accessTtlSeconds());
    }

    @Override
    @LogMethod(value = "Обновление пары токенов", maskArgs = {"rawRefreshToken"})
    public AuthResponse refresh(String rawRefreshToken) {
        RefreshTokenService.RotatedToken rotated = refreshTokenService.rotate(rawRefreshToken);
        User user = userRepository.findById(rotated.userId())
                .orElseThrow(() -> new InvalidCredentialsException("Пользователь не существует"));
        if (!Boolean.TRUE.equals(user.getActive())) {
            refreshTokenService.revoke(rotated.newRawToken());
            throw new InvalidCredentialsException("Учётная запись деактивирована");
        }
        Set<RoleCode> roles = user.getRoles().stream().map(Role::getCode).collect(Collectors.toUnmodifiableSet());
        String access = jwtService.generateAccessToken(user.getId(), user.getEmail(), roles);
        return new AuthResponse(access, rotated.newRawToken(), jwtService.accessTtlSeconds());
    }

    @Override
    @LogMethod(value = "Выход пользователя", logArgs = {"userId"}, maskArgs = {"rawRefreshToken"})
    public void logout(String rawRefreshToken, UUID userId) {
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            refreshTokenService.revoke(rawRefreshToken);
        } else if (userId != null) {
            refreshTokenService.revokeAllForUser(userId);
        }
        log.info(LogPatterns.LOGOUT_SUCCESS, userId);
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }
}
