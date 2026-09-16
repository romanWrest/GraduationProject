package ru.dstu.dormitory.auth_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.dstu.dormitory.auth_service.domain.model.Role;
import ru.dstu.dormitory.auth_service.domain.model.RoleCode;
import ru.dstu.dormitory.auth_service.domain.model.User;
import ru.dstu.dormitory.auth_service.domain.repo.UserRepository;
import ru.dstu.dormitory.auth_service.exception.InvalidCredentialsException;
import ru.dstu.dormitory.auth_service.service.Impl.AuthServiceImpl;
import ru.dstu.dormitory.auth_service.web.dto.request.LoginRequest;
import ru.dstu.dormitory.auth_service.web.dto.response.AuthResponse;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock RefreshTokenService refreshTokenService;
    @Mock RateLimiterService rateLimiterService;
    @Mock AuditService auditService;

    AuthServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AuthServiceImpl(userRepository, passwordEncoder, jwtService,
                refreshTokenService, rateLimiterService, auditService);
    }

    @Test
    void login_wrongPassword_throwsInvalidCreds() {
        User user = User.builder().id(UUID.randomUUID())
                .email("a@b.c").passwordHash("hash").active(true)
                .roles(new HashSet<>()).build();
        when(userRepository.findByEmailIgnoreCase("a@b.c")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad", "hash")).thenReturn(false);

        assertThatThrownBy(() -> service.login(new LoginRequest("a@b.c", "bad"), "1.2.3.4"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_inactive_throwsInvalidCreds() {
        User user = User.builder().id(UUID.randomUUID())
                .email("a@b.c").passwordHash("hash").active(false)
                .roles(new HashSet<>()).build();
        when(userRepository.findByEmailIgnoreCase("a@b.c")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("good", "hash")).thenReturn(true);

        assertThatThrownBy(() -> service.login(new LoginRequest("a@b.c", "good"), "1.2.3.4"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_unknownEmail_throwsInvalidCreds() {
        when(userRepository.findByEmailIgnoreCase("ghost@b.c")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginRequest("ghost@b.c", "any"), "1.2.3.4"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_ok_returnsAuthResponse() {
        UUID id = UUID.randomUUID();
        Role admin = Role.builder().id((short) 1).code(RoleCode.ADMIN).build();
        User user = User.builder().id(id).email("a@b.c").passwordHash("hash")
                .active(true).roles(new HashSet<>(Set.of(admin))).build();
        when(userRepository.findByEmailIgnoreCase("a@b.c")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("good", "hash")).thenReturn(true);
        when(jwtService.generateAccessToken(any(), anyString(), any())).thenReturn("access");
        when(refreshTokenService.issueNew(id)).thenReturn(new RefreshTokenService.IssuedToken("refresh", UUID.randomUUID()));
        when(jwtService.accessTtlSeconds()).thenReturn(900L);

        AuthResponse result = service.login(new LoginRequest("a@b.c", "good"), "1.2.3.4");

        assertThat(result.accessToken()).isEqualTo("access");
        assertThat(result.refreshToken()).isEqualTo("refresh");
        assertThat(result.expiresIn()).isEqualTo(900L);
        verify(rateLimiterService).checkLogin("1.2.3.4", "a@b.c");
    }

    @Test
    void refresh_inactiveUser_revokesNewTokenAndThrows() {
        UUID id = UUID.randomUUID();
        User user = User.builder().id(id).email("a@b.c").active(false)
                .roles(new HashSet<>()).build();
        when(refreshTokenService.rotate("rawRefresh"))
                .thenReturn(new RefreshTokenService.RotatedToken(id, "newRefresh", UUID.randomUUID()));
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.refresh("rawRefresh"))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(refreshTokenService).revoke("newRefresh");
    }

    @Test
    void logout_withToken_revokesSingle() {
        UUID id = UUID.randomUUID();
        service.logout("raw", id);
        verify(refreshTokenService).revoke("raw");
    }

    @Test
    void logout_blankToken_revokesAll() {
        UUID id = UUID.randomUUID();
        service.logout("", id);
        verify(refreshTokenService).revokeAllForUser(id);
    }
}
