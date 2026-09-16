package ru.dstu.dormitory.auth_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.dstu.dormitory.auth_service.config.PasswordProperties;
import ru.dstu.dormitory.auth_service.domain.model.PasswordResetToken;
import ru.dstu.dormitory.auth_service.domain.model.User;
import ru.dstu.dormitory.auth_service.domain.repo.PasswordResetTokenRepository;
import ru.dstu.dormitory.auth_service.domain.repo.UserRepository;
import ru.dstu.dormitory.auth_service.exception.PasswordResetTokenInvalidException;
import ru.dstu.dormitory.auth_service.exception.WeakPasswordException;
import ru.dstu.dormitory.auth_service.service.Impl.PasswordResetServiceImpl;
import ru.dstu.dormitory.auth_service.service.event.EventPublisher;
import ru.dstu.dormitory.auth_service.util.HashUtil;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceImplTest {

    @Mock UserRepository userRepository;
    @Mock PasswordResetTokenRepository tokenRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock EventPublisher eventPublisher;
    @Mock AuditService auditService;
    @Mock RefreshTokenService refreshTokenService;

    PasswordProperties passwordProperties;
    PasswordResetServiceImpl service;

    @BeforeEach
    void setUp() {
        passwordProperties = new PasswordProperties();
        service = new PasswordResetServiceImpl(userRepository, tokenRepository, passwordEncoder,
                eventPublisher, auditService, refreshTokenService, passwordProperties);
    }

    @Test
    void requestReset_unknownEmail_silentNoEventPublished() {
        when(userRepository.findByEmailIgnoreCase("ghost@example.com")).thenReturn(Optional.empty());

        assertThatCode(() -> service.requestReset("ghost@example.com"))
                .doesNotThrowAnyException();

        verify(eventPublisher, never()).publish(anyString(), anyString(), anyString(), any());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void requestReset_existingEmail_savesTokenAndPublishes() {
        UUID id = UUID.randomUUID();
        User user = User.builder().id(id).email("real@example.com").roles(new HashSet<>()).build();
        when(userRepository.findByEmailIgnoreCase("real@example.com")).thenReturn(Optional.of(user));

        service.requestReset("real@example.com");

        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(eventPublisher).publish(anyString(), anyString(), anyString(), any());
    }

    @Test
    void applyReset_weakPassword_throws() {
        assertThatThrownBy(() -> service.applyReset("token", "123"))
                .isInstanceOf(WeakPasswordException.class);
    }

    @Test
    void applyReset_unknownToken_throwsInvalid() {
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.applyReset("token", "Abcdef12345"))
                .isInstanceOf(PasswordResetTokenInvalidException.class);
    }

    @Test
    void applyReset_usedToken_throwsInvalid() {
        String raw = "my-token";
        PasswordResetToken entry = PasswordResetToken.builder()
                .tokenHash(HashUtil.sha256Hex(raw))
                .userId(UUID.randomUUID())
                .expiresAt(Instant.now().plusSeconds(600))
                .used(true)
                .build();
        when(tokenRepository.findByTokenHash(entry.getTokenHash())).thenReturn(Optional.of(entry));

        assertThatThrownBy(() -> service.applyReset(raw, "Abcdef12345"))
                .isInstanceOf(PasswordResetTokenInvalidException.class);
    }

    @Test
    void applyReset_expiredToken_throwsInvalid() {
        String raw = "my-token";
        PasswordResetToken entry = PasswordResetToken.builder()
                .tokenHash(HashUtil.sha256Hex(raw))
                .userId(UUID.randomUUID())
                .expiresAt(Instant.now().minusSeconds(60))
                .used(false)
                .build();
        when(tokenRepository.findByTokenHash(entry.getTokenHash())).thenReturn(Optional.of(entry));

        assertThatThrownBy(() -> service.applyReset(raw, "Abcdef12345"))
                .isInstanceOf(PasswordResetTokenInvalidException.class);
    }

    @Test
    void applyReset_ok_updatesPasswordAndRevokesTokens() {
        String raw = "my-token";
        UUID userId = UUID.randomUUID();
        PasswordResetToken entry = PasswordResetToken.builder()
                .tokenHash(HashUtil.sha256Hex(raw))
                .userId(userId)
                .expiresAt(Instant.now().plusSeconds(600))
                .used(false)
                .build();
        User user = User.builder().id(userId).passwordHash("old").roles(new HashSet<>()).build();
        when(tokenRepository.findByTokenHash(entry.getTokenHash())).thenReturn(Optional.of(entry));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("Abcdef12345")).thenReturn("$2a$new");

        service.applyReset(raw, "Abcdef12345");

        verify(userRepository).save(user);
        verify(refreshTokenService).revokeAllForUser(userId);
    }
}
