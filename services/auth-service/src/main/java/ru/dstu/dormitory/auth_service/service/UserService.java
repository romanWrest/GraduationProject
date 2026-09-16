package ru.dstu.dormitory.auth_service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.dstu.dormitory.auth_service.domain.model.RoleCode;
import ru.dstu.dormitory.auth_service.domain.model.User;
import ru.dstu.dormitory.auth_service.web.dto.request.CreateUserRequest;
import ru.dstu.dormitory.auth_service.web.dto.request.UpdateProfileRequest;
import ru.dstu.dormitory.auth_service.web.dto.request.UpdateUserRequest;

import java.util.Set;
import java.util.UUID;

public interface UserService {

    record CreatedUser(User user, String temporaryPassword) {
    }

    User getById(UUID id);

    User getByEmail(String email);

    CreatedUser create(UUID actorId, CreateUserRequest request);

    User updateSelf(UUID userId, UpdateProfileRequest request);

    User update(UUID actorId, UUID id, UpdateUserRequest request);

    User setStatus(UUID actorId, UUID id, boolean active);

    User setRoles(UUID actorId, UUID id, Set<RoleCode> roles);

    Page<User> search(RoleCode role, Boolean active, String search, Pageable pageable);

    void changePassword(UUID userId, String oldPassword, String newPassword);
}
