package ru.dstu.dormitory.appliances_service.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.dstu.dormitory.appliances_service.domain.enums.ApplianceStatus;
import ru.dstu.dormitory.appliances_service.domain.enums.ApplianceType;
import ru.dstu.dormitory.appliances_service.domain.enums.RoleCode;
import ru.dstu.dormitory.appliances_service.domain.model.Appliance;
import ru.dstu.dormitory.appliances_service.exception.ApplianceNotFoundException;
import ru.dstu.dormitory.appliances_service.exception.ForbiddenActionException;
import ru.dstu.dormitory.appliances_service.mapper.ApplianceMapper;
import ru.dstu.dormitory.appliances_service.security.CurrentUser;
import ru.dstu.dormitory.appliances_service.security.UserPrincipal;
import ru.dstu.dormitory.appliances_service.service.ApplianceService;
import ru.dstu.dormitory.appliances_service.service.RoomPowerService;
import ru.dstu.dormitory.appliances_service.web.dto.request.ApproveApplianceRequest;
import ru.dstu.dormitory.appliances_service.web.dto.request.RegisterApplianceRequest;
import ru.dstu.dormitory.appliances_service.web.dto.request.RejectApplianceRequest;
import ru.dstu.dormitory.appliances_service.web.dto.request.RevokeApplianceRequest;
import ru.dstu.dormitory.appliances_service.web.dto.response.ApplianceDto;
import ru.dstu.dormitory.appliances_service.web.dto.response.RoomAppliancesDto;

import java.util.UUID;

@Tag(name = "Appliances", description = "Личные электроприборы жильцов")
@RestController
@RequestMapping("/api/v1/appliances")
@RequiredArgsConstructor
public class ApplianceController {

    private final ApplianceService applianceService;
    private final RoomPowerService roomPowerService;
    private final ApplianceMapper mapper;

    @Operation(summary = "Зарегистрировать прибор (RESIDENT)")
    @PostMapping
    @PreAuthorize("hasRole('RESIDENT')")
    public ResponseEntity<ApplianceDto> register(@Valid @RequestBody RegisterApplianceRequest request,
                                                 @CurrentUser UserPrincipal principal) {
        if (principal == null) {
            throw new ForbiddenActionException("Аутентификация обязательна");
        }
        Appliance saved = applianceService.register(principal.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(saved));
    }

    @Operation(summary = "Список приборов с фильтрами. ADMIN видит всё, RESIDENT — только свои")
    @GetMapping
    public ResponseEntity<Page<ApplianceDto>> list(@RequestParam(required = false) ApplianceStatus status,
                                                   @RequestParam(required = false) UUID residentId,
                                                   @RequestParam(required = false) UUID roomId,
                                                   @RequestParam(required = false) ApplianceType type,
                                                   @RequestParam(required = false) String search,
                                                   @CurrentUser UserPrincipal principal,
                                                   Pageable pageable) {
        if (principal == null) {
            throw new ForbiddenActionException("Аутентификация обязательна");
        }
        UUID effectiveUserId = isAdmin(principal) ? null : principal.userId();
        UUID effectiveResidentId = isAdmin(principal) ? residentId : null;
        UUID effectiveRoomId = isAdmin(principal) ? roomId : null;
        Page<Appliance> page = applianceService.search(status, effectiveResidentId, effectiveUserId,
                effectiveRoomId, type, search, pageable);
        return ResponseEntity.ok(page.map(mapper::toDto));
    }

    @Operation(summary = "Мои приборы (RESIDENT)")
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ApplianceDto>> my(@RequestParam(required = false) ApplianceStatus status,
                                                 @CurrentUser UserPrincipal principal,
                                                 Pageable pageable) {
        if (principal == null) {
            throw new ForbiddenActionException("Аутентификация обязательна");
        }
        Page<Appliance> page = applianceService.search(status, null, principal.userId(),
                null, null, null, pageable);
        return ResponseEntity.ok(page.map(mapper::toDto));
    }

    @Operation(summary = "Получить прибор по id")
    @GetMapping("/{id}")
    public ResponseEntity<ApplianceDto> get(@PathVariable UUID id,
                                            @CurrentUser UserPrincipal principal) {
        Appliance appliance = applianceService.getById(id);
        ensureCanRead(appliance, principal);
        return ResponseEntity.ok(mapper.toDto(appliance));
    }

    @Operation(summary = "Одобрить прибор (ADMIN). Проверяет лимит мощности комнаты")
    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApplianceDto> approve(@PathVariable UUID id,
                                                @Valid @RequestBody(required = false) ApproveApplianceRequest request,
                                                @CurrentUser UserPrincipal principal) {
        UUID actorId = principal != null ? principal.userId() : null;
        String comment = request != null ? request.comment() : null;
        Appliance updated = applianceService.approve(id, actorId, comment);
        return ResponseEntity.ok(mapper.toDto(updated));
    }

    @Operation(summary = "Отклонить прибор (ADMIN)")
    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApplianceDto> reject(@PathVariable UUID id,
                                               @Valid @RequestBody RejectApplianceRequest request,
                                               @CurrentUser UserPrincipal principal) {
        UUID actorId = principal != null ? principal.userId() : null;
        Appliance updated = applianceService.reject(id, actorId, request.reason());
        return ResponseEntity.ok(mapper.toDto(updated));
    }

    @Operation(summary = "Снять прибор с учёта (ADMIN)")
    @PatchMapping("/{id}/revoke")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApplianceDto> revoke(@PathVariable UUID id,
                                               @Valid @RequestBody RevokeApplianceRequest request,
                                               @CurrentUser UserPrincipal principal) {
        UUID actorId = principal != null ? principal.userId() : null;
        Appliance updated = applianceService.revoke(id, actorId, request.reason());
        return ResponseEntity.ok(mapper.toDto(updated));
    }

    @Operation(summary = "Приборы комнаты с суммарной мощностью (ADMIN)")
    @GetMapping("/by-room/{roomId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomAppliancesDto> byRoom(@PathVariable UUID roomId) {
        return ResponseEntity.ok(buildRoomDto(roomId));
    }

    private RoomAppliancesDto buildRoomDto(UUID roomId) {
        var appliances = applianceService.listApprovedByRoom(roomId);
        int total = appliances.stream().mapToInt(Appliance::getPowerWatts).sum();
        return new RoomAppliancesDto(roomId, total, roomPowerService.powerLimitWatts(),
                mapper.toDtoList(appliances));
    }

    private void ensureCanRead(Appliance appliance, UserPrincipal principal) {
        if (principal == null) {
            throw new ApplianceNotFoundException("Прибор не найден: id=" + appliance.getId());
        }
        if (isAdmin(principal)) {
            return;
        }
        if (!appliance.getUserId().equals(principal.userId())) {
            throw new ApplianceNotFoundException("Прибор не найден: id=" + appliance.getId());
        }
    }

    private boolean isAdmin(UserPrincipal principal) {
        return principal.roles().contains(RoleCode.ADMIN);
    }
}
