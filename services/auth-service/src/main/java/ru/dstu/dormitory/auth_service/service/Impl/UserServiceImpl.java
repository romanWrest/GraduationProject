package ru.dstu.dormitory.auth_service.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.auth_service.aspect.annotation.LogMethod;
import ru.dstu.dormitory.auth_service.domain.model.Role;
import ru.dstu.dormitory.auth_service.domain.model.RoleCode;
import ru.dstu.dormitory.auth_service.domain.model.User;
import ru.dstu.dormitory.auth_service.domain.repo.RoleRepository;
import ru.dstu.dormitory.auth_service.domain.repo.UserRepository;
import ru.dstu.dormitory.auth_service.domain.repo.UserSpecifications;
import ru.dstu.dormitory.auth_service.exception.InvalidCredentialsException;
import ru.dstu.dormitory.auth_service.exception.UserAlreadyExistsException;
import ru.dstu.dormitory.auth_service.exception.UserNotFoundException;
import ru.dstu.dormitory.auth_service.service.AuditService;
import ru.dstu.dormitory.auth_service.service.RefreshTokenService;
import ru.dstu.dormitory.auth_service.service.UserService;
import ru.dstu.dormitory.auth_service.service.event.EventPublisher;
import ru.dstu.dormitory.auth_service.service.event.EventTypes;
import ru.dstu.dormitory.auth_service.service.event.UserCreatedEvent;
import ru.dstu.dormitory.auth_service.service.event.UserDeactivatedEvent;
import ru.dstu.dormitory.auth_service.util.HashUtil;
import ru.dstu.dormitory.auth_service.util.LogPatterns;
import ru.dstu.dormitory.auth_service.util.PasswordPolicy;
import ru.dstu.dormitory.auth_service.web.dto.request.CreateUserRequest;
import ru.dstu.dormitory.auth_service.web.dto.request.UpdateProfileRequest;
import ru.dstu.dormitory.auth_service.web.dto.request.UpdateUserRequest;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String TARGET_USER = "User";
    private static final int GENERATED_PASSWORD_BYTES = 12;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EventPublisher eventPublisher;
    private final AuditService auditService;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional(readOnly = true)
    @LogMethod(value = "Получение пользователя по id", logArgs = {"id"})
    public User getById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден: id=%s".formatted(id)));
    }

    @Override
    @Transactional(readOnly = true)
    @LogMethod(value = "Получение пользователя по email", logArgs = {"email"})
    public User getByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден: email=%s".formatted(email)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> search(RoleCode role, Boolean active, String search, Pageable pageable) {
        return userRepository.findAll(
                UserSpecifications.search(role, active, normalize(search)),
                pageable);
    }

    @Override
    @Transactional
    @LogMethod(value = "Создание пользователя", logArgs = {"request"})
    public CreatedUser create(UUID actorId, CreateUserRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new UserAlreadyExistsException("Email уже занят: %s".formatted(request.email()));
        }
        String tempPassword = HashUtil.randomBase64Url(GENERATED_PASSWORD_BYTES);
        Set<Role> rolesEntities = resolveRoles(request.roles());

        User user = User.builder()
                .email(request.email().toLowerCase())
                .passwordHash(passwordEncoder.encode(tempPassword))
                .fullName(request.fullName())
                .phone(request.phone())
                .active(true)
                .roles(new HashSet<>(rolesEntities))
                .build();

        User saved = userRepository.save(user);
        eventPublisher.publish(EventTypes.AGGREGATE_USER, saved.getId().toString(), EventTypes.USER_CREATED,
                new UserCreatedEvent(saved.getId(), saved.getEmail(), saved.getFullName(),
                        request.roles(), tempPassword, Instant.now()));
        auditService.record(actorId, AuditService.Action.USER_CREATE, TARGET_USER, saved.getId().toString(),
                Map.of("email", saved.getEmail(), "roles", request.roles().stream().map(Enum::name).toList()));
        log.info(LogPatterns.USER_CREATED, saved.getId(), saved.getEmail());
        return new CreatedUser(saved, tempPassword);
    }

    @Override
    @Transactional
    @LogMethod(value = "Обновление профиля пользователем", logArgs = {"userId", "request"})
    public User updateSelf(UUID userId, UpdateProfileRequest request) {
        User user = getById(userId);
        applyProfileUpdate(user, request.fullName(), request.phone(), request.email());
        userRepository.save(user);
        auditService.record(userId, AuditService.Action.USER_UPDATE, TARGET_USER, userId.toString(),
                Map.of("self", true));
        log.info(LogPatterns.USER_UPDATED, user.getId());
        return user;
    }

    @Override
    @Transactional
    @LogMethod(value = "Админ-обновление пользователя", logArgs = {"id", "request"})
    public User update(UUID actorId, UUID id, UpdateUserRequest request) {
        User user = getById(id);
        applyProfileUpdate(user, request.fullName(), request.phone(), request.email());
        userRepository.save(user);
        auditService.record(actorId, AuditService.Action.USER_UPDATE, TARGET_USER, id.toString(), Map.of());
        log.info(LogPatterns.USER_UPDATED, user.getId());
        return user;
    }

    @Override
    @Transactional
    @LogMethod(value = "Смена статуса пользователя", logArgs = {"id", "active"})
    public User setStatus(UUID actorId, UUID id, boolean active) {
        User user = getById(id);
        boolean wasActive = Boolean.TRUE.equals(user.getActive());
        user.setActive(active);
        userRepository.save(user);
        if (wasActive && !active) {
            refreshTokenService.revokeAllForUser(id);
            eventPublisher.publish(EventTypes.AGGREGATE_USER, id.toString(), EventTypes.USER_DEACTIVATED,
                    new UserDeactivatedEvent(id, Instant.now()));
            auditService.record(actorId, AuditService.Action.USER_DEACTIVATE, TARGET_USER, id.toString(), Map.of());
            log.info(LogPatterns.USER_DEACTIVATED, id);
        } else if (!wasActive && active) {
            auditService.record(actorId, AuditService.Action.USER_UPDATE, TARGET_USER, id.toString(),
                    Map.of("reactivated", true));
            log.info(LogPatterns.USER_ACTIVATED, id);
        }
        return user;
    }

    @Override
    @Transactional
    @LogMethod(value = "Изменение ролей пользователя", logArgs = {"id", "roles"})
    public User setRoles(UUID actorId, UUID id, Set<RoleCode> roles) {
        User user = getById(id);
        user.setRoles(new HashSet<>(resolveRoles(roles)));
        userRepository.save(user);
        auditService.record(actorId, AuditService.Action.ROLES_CHANGE, TARGET_USER, id.toString(),
                Map.of("roles", roles.stream().map(Enum::name).toList()));
        log.info(LogPatterns.USER_ROLES_CHANGED, id, roles);
        return user;
    }

    @Override
    @Transactional
    @LogMethod(value = "Смена пароля", logArgs = {"userId"}, maskArgs = {"oldPassword", "newPassword"})
    public void changePassword(UUID userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Текущий пароль указан неверно");
        }
        PasswordPolicy.validate(newPassword);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        refreshTokenService.revokeAllForUser(userId);
        auditService.record(userId, AuditService.Action.PASSWORD_CHANGE, TARGET_USER, userId.toString(), Map.of());
        log.info(LogPatterns.PASSWORD_CHANGED, userId);
    }

    private Set<Role> resolveRoles(Set<RoleCode> codes) {
        Set<Role> found = roleRepository.findAllByCodeIn(codes);
        if (found.size() != codes.size()) {
            Set<RoleCode> foundCodes = found.stream().map(Role::getCode).collect(Collectors.toSet());
            throw new IllegalArgumentException("Неизвестные коды ролей: %s"
                    .formatted(codes.stream().filter(c -> !foundCodes.contains(c)).toList()));
        }
        return found;
    }

    private void applyProfileUpdate(User user, String fullName, String phone, String email) {
        if (fullName != null && !fullName.isBlank()) {
            user.setFullName(fullName);
        }
        if (phone != null) {
            user.setPhone(phone.isBlank() ? null : phone);
        }
        if (email != null && !email.isBlank() && !email.equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmailIgnoreCase(email)) {
                throw new UserAlreadyExistsException("Email уже занят: %s".formatted(email));
            }
            user.setEmail(email.toLowerCase());
        }
    }

    private String normalize(String search) {
        return search == null || search.isBlank() ? null : search.trim();
    }
}
