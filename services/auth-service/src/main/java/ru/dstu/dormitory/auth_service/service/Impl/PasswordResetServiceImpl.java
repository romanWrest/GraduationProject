package ru.dstu.dormitory.auth_service.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.auth_service.aspect.annotation.LogMethod;
import ru.dstu.dormitory.auth_service.config.PasswordProperties;
import ru.dstu.dormitory.auth_service.domain.model.PasswordResetToken;
import ru.dstu.dormitory.auth_service.domain.model.User;
import ru.dstu.dormitory.auth_service.domain.repo.PasswordResetTokenRepository;
import ru.dstu.dormitory.auth_service.domain.repo.UserRepository;
import ru.dstu.dormitory.auth_service.exception.PasswordResetTokenInvalidException;
import ru.dstu.dormitory.auth_service.service.AuditService;
import ru.dstu.dormitory.auth_service.service.PasswordResetService;
import ru.dstu.dormitory.auth_service.service.RefreshTokenService;
import ru.dstu.dormitory.auth_service.service.event.EventPublisher;
import ru.dstu.dormitory.auth_service.service.event.EventTypes;
import ru.dstu.dormitory.auth_service.service.event.PasswordResetRequestedEvent;
import ru.dstu.dormitory.auth_service.util.HashUtil;
import ru.dstu.dormitory.auth_service.util.LogPatterns;
import ru.dstu.dormitory.auth_service.util.PasswordPolicy;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final String TARGET_USER = "User";

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EventPublisher eventPublisher;
    private final AuditService auditService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordProperties passwordProperties;

    @Override
    @Transactional
    @LogMethod(value = "Запрос сброса пароля", logArgs = {"email"})
    public void requestReset(String email) {
        Optional<User> maybeUser = userRepository.findByEmailIgnoreCase(email);
        log.info(LogPatterns.PASSWORD_RESET_REQUESTED, email);
        if (maybeUser.isEmpty()) {
            return;
        }
        User user = maybeUser.get();

        String rawToken = UUID.randomUUID().toString();
        String tokenHash = HashUtil.sha256Hex(rawToken);
        Duration ttl = Duration.ofMinutes(passwordProperties.getResetTtlMinutes());
        Instant expiresAt = Instant.now().plus(ttl);

        PasswordResetToken entry = PasswordResetToken.builder()
                .tokenHash(tokenHash)
                .userId(user.getId())
                .expiresAt(expiresAt)
                .used(false)
                .build();
        tokenRepository.save(entry);

        eventPublisher.publish(EventTypes.AGGREGATE_USER, user.getId().toString(),
                EventTypes.PASSWORD_RESET_REQUESTED,
                new PasswordResetRequestedEvent(user.getId(), user.getEmail(), rawToken, expiresAt));
        auditService.record(user.getId(), AuditService.Action.PASSWORD_RESET_REQUEST,
                TARGET_USER, user.getId().toString(), Map.of("email", user.getEmail()));
    }

    @Override
    @Transactional
    @LogMethod(value = "Применение сброса пароля", maskArgs = {"token", "newPassword"})
    public void applyReset(String token, String newPassword) {
        PasswordPolicy.validate(newPassword);
        String tokenHash = HashUtil.sha256Hex(token);
        PasswordResetToken entry = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new PasswordResetTokenInvalidException("Токен сброса недействителен"));

        if (Boolean.TRUE.equals(entry.getUsed())) {
            throw new PasswordResetTokenInvalidException("Токен уже использован");
        }
        if (entry.getExpiresAt().isBefore(Instant.now())) {
            throw new PasswordResetTokenInvalidException("Токен просрочен");
        }

        User user = userRepository.findById(entry.getUserId())
                .orElseThrow(() -> new PasswordResetTokenInvalidException("Пользователь удалён"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        entry.setUsed(true);
        tokenRepository.save(entry);

        refreshTokenService.revokeAllForUser(user.getId());

        auditService.record(user.getId(), AuditService.Action.PASSWORD_CHANGE,
                TARGET_USER, user.getId().toString(), Map.of("via", "reset"));
        log.info(LogPatterns.PASSWORD_RESET_COMPLETED, user.getId());
    }
}
