package ru.dstu.dormitory.notifications_service.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.dstu.dormitory.notifications_service.mapper.NotificationMapper;
import ru.dstu.dormitory.notifications_service.security.CurrentUser;
import ru.dstu.dormitory.notifications_service.security.UserPrincipal;
import ru.dstu.dormitory.notifications_service.service.SettingsService;
import ru.dstu.dormitory.notifications_service.web.dto.NotificationSettingsDto;
import ru.dstu.dormitory.notifications_service.web.dto.UpdateSettingsDto;

@Tag(name = "Settings", description = "Настройки каналов уведомлений")
@RestController
@RequestMapping("/api/v1/notifications/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService service;
    private final NotificationMapper mapper;

    @Operation(summary = "Получить мои настройки уведомлений")
    @GetMapping
    public ResponseEntity<NotificationSettingsDto> get(@CurrentUser UserPrincipal principal) {
        requirePrincipal(principal);
        return ResponseEntity.ok(mapper.toDto(service.getOrDefault(principal.userId())));
    }

    @Operation(summary = "Обновить мои настройки уведомлений")
    @PatchMapping
    public ResponseEntity<NotificationSettingsDto> update(@CurrentUser UserPrincipal principal,
                                                          @RequestBody UpdateSettingsDto dto) {
        requirePrincipal(principal);
        return ResponseEntity.ok(mapper.toDto(service.update(principal.userId(), dto)));
    }

    private void requirePrincipal(UserPrincipal principal) {
        if (principal == null) {
            throw new AccessDeniedException("Требуется аутентификация");
        }
    }
}
