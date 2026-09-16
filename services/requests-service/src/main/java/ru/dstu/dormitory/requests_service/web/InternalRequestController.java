package ru.dstu.dormitory.requests_service.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.dstu.dormitory.requests_service.mapper.RequestMapper;
import ru.dstu.dormitory.requests_service.service.RequestService;
import ru.dstu.dormitory.requests_service.web.dto.RequestDto;

import java.util.UUID;

@Tag(name = "Internal", description = "Служебные эндпоинты для межсервисного взаимодействия")
@RestController
@RequestMapping("/api/v1/internal")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SERVICE', 'ADMIN')")
public class InternalRequestController {

    private final RequestService requestService;
    private final RequestMapper requestMapper;

    @Operation(summary = "Получить заявку по id (service-to-service)")
    @GetMapping("/requests/{id}")
    public ResponseEntity<RequestDto> get(@PathVariable UUID id) {
        return ResponseEntity.ok(requestMapper.toDto(requestService.getById(id)));
    }
}
