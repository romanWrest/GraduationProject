package ru.dstu.dormitory.notifications_service.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.dstu.dormitory.notifications_service.domain.enums.NotificationChannel;
import ru.dstu.dormitory.notifications_service.mapper.NotificationMapper;
import ru.dstu.dormitory.notifications_service.service.DispatchService;
import ru.dstu.dormitory.notifications_service.web.dto.InternalCreateNotificationDto;
import ru.dstu.dormitory.notifications_service.web.dto.NotificationDto;

@Tag(name = "Internal", description = "Служебные эндпоинты для других сервисов")
@RestController
@RequestMapping("/api/v1/internal/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SERVICE', 'ADMIN')")
public class InternalController {

    private final DispatchService dispatchService;
    private final NotificationMapper mapper;

    @Operation(summary = "Прямая отправка уведомления (sync, мимо Kafka)")
    @PostMapping
    public ResponseEntity<NotificationDto> create(@Valid @RequestBody InternalCreateNotificationDto dto) {
        var notification = dispatchService.dispatchDirect(
                dto.userId(),
                dto.type(),
                dto.title(),
                dto.body(),
                dto.payload(),
                dto.channels(),
                dto.channels().contains(NotificationChannel.EMAIL) ? "internal-direct" : null,
                dto.channels().contains(NotificationChannel.EMAIL) ? dto.body() : null);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(notification));
    }
}
