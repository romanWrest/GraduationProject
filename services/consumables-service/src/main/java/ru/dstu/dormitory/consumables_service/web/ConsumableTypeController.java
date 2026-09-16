package ru.dstu.dormitory.consumables_service.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.dstu.dormitory.consumables_service.domain.model.ConsumableType;
import ru.dstu.dormitory.consumables_service.mapper.ConsumableMapper;
import ru.dstu.dormitory.consumables_service.security.CurrentUser;
import ru.dstu.dormitory.consumables_service.security.UserPrincipal;
import ru.dstu.dormitory.consumables_service.service.ConsumableTypeService;
import ru.dstu.dormitory.consumables_service.service.StockService;
import ru.dstu.dormitory.consumables_service.web.dto.request.CreateConsumableTypeRequest;
import ru.dstu.dormitory.consumables_service.web.dto.request.StockOperationRequest;
import ru.dstu.dormitory.consumables_service.web.dto.request.UpdateConsumableTypeRequest;
import ru.dstu.dormitory.consumables_service.web.dto.response.ConsumableTypeDto;

import java.util.List;
import java.util.UUID;

@Tag(name = "ConsumableTypes", description = "Справочник типов расходников")
@RestController
@RequestMapping("/api/v1/consumables/types")
@RequiredArgsConstructor
public class ConsumableTypeController {

    private final ConsumableTypeService typeService;
    private final StockService stockService;
    private final ConsumableMapper mapper;

    @Operation(summary = "Получить все типы расходников")
    @GetMapping
    public ResponseEntity<List<ConsumableTypeDto>> list() {
        return ResponseEntity.ok(mapper.toDtoList(typeService.listAll()));
    }

    @Operation(summary = "Создать тип расходника (PROPERTY_MANAGER, ADMIN)")
    @PostMapping
    @PreAuthorize("hasAnyRole('PROPERTY_MANAGER', 'ADMIN')")
    public ResponseEntity<ConsumableTypeDto> create(@Valid @RequestBody CreateConsumableTypeRequest request) {
        ConsumableType created = typeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(created));
    }

    @Operation(summary = "Обновить тип расходника (PROPERTY_MANAGER, ADMIN)")
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROPERTY_MANAGER', 'ADMIN')")
    public ResponseEntity<ConsumableTypeDto> update(@PathVariable UUID id,
                                                    @Valid @RequestBody UpdateConsumableTypeRequest request) {
        ConsumableType updated = typeService.update(id, request);
        return ResponseEntity.ok(mapper.toDto(updated));
    }

    @Operation(summary = "Изменить остаток (PROPERTY_MANAGER, ADMIN). delta > 0 — пополнение, < 0 — списание")
    @PostMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('PROPERTY_MANAGER', 'ADMIN')")
    public ResponseEntity<ConsumableTypeDto> changeStock(@PathVariable UUID id,
                                                         @Valid @RequestBody StockOperationRequest request,
                                                         @CurrentUser UserPrincipal principal) {
        UUID actorId = principal != null ? principal.userId() : null;
        ConsumableType updated = stockService.adjustStock(id, request.delta(), request.reason(), actorId);
        return ResponseEntity.ok(mapper.toDto(updated));
    }
}
