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
import ru.dstu.dormitory.auth_service.domain.repo.RoleRepository;
import ru.dstu.dormitory.auth_service.domain.repo.UserRepository;
import ru.dstu.dormitory.auth_service.exception.InvalidCredentialsException;
import ru.dstu.dormitory.auth_service.exception.UserAlreadyExistsException;
import ru.dstu.dormitory.auth_service.exception.UserNotFoundException;
import ru.dstu.dormitory.auth_service.exception.WeakPasswordException;
import ru.dstu.dormitory.auth_service.service.Impl.UserServiceImpl;
import ru.dstu.dormitory.auth_service.service.event.EventPublisher;
import ru.dstu.dormitory.auth_service.web.dto.request.CreateUserRequest;
import ru.dstu.dormitory.auth_service.web.dto.request.UpdateProfileRequest;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock UserRepository userRepository;
    @Mock RoleRepository roleRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock EventPublisher eventPublisher;
    @Mock AuditService auditService;
    @Mock RefreshTokenService refreshTokenService;

    UserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl(userRepository, roleRepository, passwordEncoder,
                eventPublisher, auditService, refreshTokenService);
    }

    @Test
    void getById_missing_throws() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void create_duplicateEmail_throwsAlreadyExists() {
        CreateUserRequest req = new CreateUserRequest("dup@example.com", "Иван", null, Set.of(RoleCode.RESIDENT));
        when(userRepository.existsByEmailIgnoreCase("dup@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.create(UUID.randomUUID(), req))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    void create_ok_publishesEventAndReturnsTempPassword() {
        CreateUserRequest req = new CreateUserRequest("new@example.com", "Мария", "+79001112233",
                Set.of(RoleCode.RESIDENT));
        Role resident = Role.builder().id((short) 2).code(RoleCode.RESIDENT).build();
        when(userRepository.existsByEmailIgnoreCase("new@example.com")).thenReturn(false);
        when(roleRepository.findAllByCodeIn(req.roles())).thenReturn(new HashSet<>(Set.of(resident)));
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            if (u.getId() == null) u.setId(UUID.randomUUID());
            return u;
        });

        UserService.CreatedUser created = service.create(UUID.randomUUID(), req);

        assertThat(created.temporaryPassword()).isNotBlank();
        assertThat(created.user().getEmail()).isEqualTo("new@example.com");
        verify(eventPublisher).publish(anyString(), anyString(), anyString(), any());
    }

    @Test
    void updateSelf_updatesFields() {
        UUID id = UUID.randomUUID();
        User existing = User.builder()
                .id(id)
                .email("old@example.com")
                .fullName("Old Name")
                .active(true)
                .roles(new HashSet<>())
                .build();
        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User updated = service.updateSelf(id, new UpdateProfileRequest("Новое Имя", "+79000000000", null));

        assertThat(updated.getFullName()).isEqualTo("Новое Имя");
        assertThat(updated.getPhone()).isEqualTo("+79000000000");
    }

    @Test
    void setStatus_deactivate_revokesRefreshAndPublishes() {
        UUID id = UUID.randomUUID();
        User user = User.builder().id(id).email("x@y.z").active(true).roles(new HashSet<>()).build();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        service.setStatus(UUID.randomUUID(), id, false);

        verify(refreshTokenService).revokeAllForUser(id);
        verify(eventPublisher).publish(anyString(), eq(id.toString()), anyString(), any());
    }

    @Test
    void setStatus_alreadyInactive_doesNotRevoke() {
        UUID id = UUID.randomUUID();
        User user = User.builder().id(id).email("x@y.z").active(false).roles(new HashSet<>()).build();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        service.setStatus(UUID.randomUUID(), id, false);

        verify(refreshTokenService, never()).revokeAllForUser(any());
    }

    @Test
    void changePassword_invalidOld_throws() {
        UUID id = UUID.randomUUID();
        User user = User.builder().id(id).passwordHash("hash").roles(new HashSet<>()).build();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertThatThrownBy(() -> service.changePassword(id, "wrong", "Qwerty12345"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void changePassword_weakNew_throws() {
        UUID id = UUID.randomUUID();
        User user = User.builder().id(id).passwordHash("hash").roles(new HashSet<>()).build();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "hash")).thenReturn(true);

        assertThatThrownBy(() -> service.changePassword(id, "old", "123"))
                .isInstanceOf(WeakPasswordException.class);
    }

    @Test
    void changePassword_ok_revokesAllTokens() {
        UUID id = UUID.randomUUID();
        User user = User.builder().id(id).passwordHash("hash").roles(new HashSet<>()).build();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "hash")).thenReturn(true);
        when(passwordEncoder.encode("NewPass123")).thenReturn("$2a$newhash");

        service.changePassword(id, "old", "NewPass123");

        assertThat(user.getPasswordHash()).isEqualTo("$2a$newhash");
        verify(refreshTokenService).revokeAllForUser(id);
    }
}
