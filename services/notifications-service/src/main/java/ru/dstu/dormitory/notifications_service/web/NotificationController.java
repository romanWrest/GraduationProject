package ru.dstu.dormitory.notifications_service.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.dstu.dormitory.notifications_service.domain.enums.NotificationType;
import ru.dstu.dormitory.notifications_service.mapper.NotificationMapper;
import ru.dstu.dormitory.notifications_service.security.CurrentUser;
import ru.dstu.dormitory.notifications_service.security.UserPrincipal;
import ru.dstu.dormitory.notifications_service.service.NotificationService;
import ru.dstu.dormitory.notifications_service.web.dto.AffectedDto;
import ru.dstu.dormitory.notifications_service.web.dto.NotificationDto;
import ru.dstu.dormitory.notifications_service.web.dto.UnreadCountDto;

import java.time.Instant;
import java.util.UUID;

@Tag(name = "Notifications", description = "In-app уведомления")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;
    private final NotificationMapper mapper;

    @Operation(summary = "Список моих уведомлений с фильтрами")
    @GetMapping
    public ResponseEntity<Page<NotificationDto>> list(
            @CurrentUser UserPrincipal principal,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @PageableDefault(size = 20) Pageable pageable) {
        requirePrincipal(principal);
        return ResponseEntity.ok(
                service.search(principal.userId(), unreadOnly, type, from, to, pageable)
                        .map(mapper::toDto));
    }

    @Operation(summary = "Количество непрочитанных уведомлений")
    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountDto> unreadCount(@CurrentUser UserPrincipal principal) {
        requirePrincipal(principal);
        return ResponseEntity.ok(new UnreadCountDto(service.countUnread(principal.userId())));
    }

    @Operation(summary = "Пометить уведомление прочитанным")
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@CurrentUser UserPrincipal principal,
                                         @PathVariable UUID id) {
        requirePrincipal(principal);
        service.markRead(id, principal.userId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Пометить все мои уведомления прочитанными")
    @PatchMapping("/read-all")
    public ResponseEntity<AffectedDto> markAllRead(@CurrentUser UserPrincipal principal) {
        requirePrincipal(principal);
        return ResponseEntity.ok(new AffectedDto(service.markAllRead(principal.userId())));
    }

    private void requirePrincipal(UserPrincipal principal) {
        if (principal == null) {
            throw new org.springframework.security.access.AccessDeniedException("Требуется аутентификация");
        }
    }
}
