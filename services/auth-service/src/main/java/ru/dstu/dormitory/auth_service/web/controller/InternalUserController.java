package ru.dstu.dormitory.auth_service.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.dstu.dormitory.auth_service.mapper.UserMapper;
import ru.dstu.dormitory.auth_service.service.UserService;
import ru.dstu.dormitory.auth_service.web.dto.request.BatchUserIdsRequest;
import ru.dstu.dormitory.auth_service.web.dto.response.UserDto;

import java.util.List;
import java.util.UUID;

@Tag(name = "Internal", description = "Служебные эндпоинты для межсервисного взаимодействия")
@RestController
@RequestMapping("/api/v1/internal")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SERVICE', 'ADMIN')")
public class InternalUserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @Operation(summary = "Получить пользователя по id (service-to-service)")
    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(userMapper.toDto(userService.getById(id)));
    }

    @Operation(summary = "Batch-получение пользователей по списку id")
    @PostMapping("/users/batch")
    public ResponseEntity<List<UserDto>> batch(@Valid @RequestBody BatchUserIdsRequest request) {
        List<UserDto> result = request.ids().stream()
                .map(userService::getById)
                .map(userMapper::toDto)
                .toList();
        return ResponseEntity.ok(result);
    }
}
