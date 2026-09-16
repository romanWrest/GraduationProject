package ru.dstu.dormitory.auth_service.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.dstu.dormitory.auth_service.domain.model.RoleCode;
import ru.dstu.dormitory.auth_service.exception.UserNotFoundException;
import ru.dstu.dormitory.auth_service.mapper.UserMapper;
import ru.dstu.dormitory.auth_service.security.CurrentUser;
import ru.dstu.dormitory.auth_service.security.UserPrincipal;
import ru.dstu.dormitory.auth_service.service.UserService;
import ru.dstu.dormitory.auth_service.web.dto.request.CreateUserRequest;
import ru.dstu.dormitory.auth_service.web.dto.request.UpdateProfileRequest;
import ru.dstu.dormitory.auth_service.web.dto.request.UpdateUserRequest;
import ru.dstu.dormitory.auth_service.web.dto.request.UpdateUserRolesRequest;
import ru.dstu.dormitory.auth_service.web.dto.request.UpdateUserStatusRequest;
import ru.dstu.dormitory.auth_service.web.dto.response.CreatedUserDto;
import ru.dstu.dormitory.auth_service.web.dto.response.UserDto;

import java.util.UUID;

@Tag(name = "Users", description = "Управление пользователями")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @Operation(summary = "Профиль текущего пользователя")
    @GetMapping("/me")
    public ResponseEntity<UserDto> me(@CurrentUser UserPrincipal principal) {
        requirePrincipal(principal);
        return ResponseEntity.ok(userMapper.toDto(userService.getById(principal.userId())));
    }

    @Operation(summary = "Обновить свой профиль")
    @PutMapping("/me")
    public ResponseEntity<UserDto> updateSelf(@CurrentUser UserPrincipal principal,
                                              @Valid @RequestBody UpdateProfileRequest request) {
        requirePrincipal(principal);
        return ResponseEntity.ok(userMapper.toDto(userService.updateSelf(principal.userId(), request)));
    }

    @Operation(summary = "Поиск пользователей (ADMIN)")
    @GetMapping
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDto>> search(@RequestParam(required = false) RoleCode role,
                                                @RequestParam(required = false) Boolean active,
                                                @RequestParam(required = false) String q,
                                                @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(userService.search(role, active, q, pageable).map(userMapper::toDto));
    }

    @Operation(summary = "Получить пользователя по id (ADMIN)")
    @GetMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(userMapper.toDto(userService.getById(id)));
    }

    @Operation(summary = "Создать пользователя (ADMIN)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CreatedUserDto> create(@CurrentUser UserPrincipal principal,
                                                 @Valid @RequestBody CreateUserRequest request) {
        requirePrincipal(principal);
        UserService.CreatedUser created = userService.create(principal.userId(), request);
        UserDto dto = userMapper.toDto(created.user());
        return ResponseEntity.status(201).body(new CreatedUserDto(dto, created.temporaryPassword()));
    }

    @Operation(summary = "Обновить пользователя (ADMIN)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> update(@CurrentUser UserPrincipal principal,
                                          @PathVariable UUID id,
                                          @Valid @RequestBody UpdateUserRequest request) {
        requirePrincipal(principal);
        return ResponseEntity.ok(userMapper.toDto(userService.update(principal.userId(), id, request)));
    }

    @Operation(summary = "Сменить статус active (ADMIN)")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> setStatus(@CurrentUser UserPrincipal principal,
                                             @PathVariable UUID id,
                                             @Valid @RequestBody UpdateUserStatusRequest request) {
        requirePrincipal(principal);
        return ResponseEntity.ok(userMapper.toDto(
                userService.setStatus(principal.userId(), id, request.active())));
    }

    @Operation(summary = "Сменить роли пользователя (ADMIN)")
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> setRoles(@CurrentUser UserPrincipal principal,
                                            @PathVariable UUID id,
                                            @Valid @RequestBody UpdateUserRolesRequest request) {
        requirePrincipal(principal);
        return ResponseEntity.ok(userMapper.toDto(
                userService.setRoles(principal.userId(), id, request.roles())));
    }

    @Operation(summary = "Деактивировать пользователя (ADMIN)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(@CurrentUser UserPrincipal principal,
                                           @PathVariable UUID id) {
        requirePrincipal(principal);
        userService.setStatus(principal.userId(), id, false);
        return ResponseEntity.noContent().build();
    }

    private void requirePrincipal(UserPrincipal principal) {
        if (principal == null) {
            throw new UserNotFoundException("Не удалось определить текущего пользователя");
        }
    }
}
