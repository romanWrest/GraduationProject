package ru.dstu.dormitory.appliances_service.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.dstu.dormitory.appliances_service.domain.model.Appliance;
import ru.dstu.dormitory.appliances_service.mapper.ApplianceMapper;
import ru.dstu.dormitory.appliances_service.service.ApplianceService;
import ru.dstu.dormitory.appliances_service.service.RoomPowerService;
import ru.dstu.dormitory.appliances_service.web.dto.response.ApplianceDto;
import ru.dstu.dormitory.appliances_service.web.dto.response.RoomAppliancesDto;

import java.util.UUID;

@Tag(name = "Internal", description = "Служебные эндпоинты для межсервисного взаимодействия")
@RestController
@RequestMapping("/api/v1/internal/appliances")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SERVICE', 'ADMIN')")
public class InternalApplianceController {

    private final ApplianceService applianceService;
    private final RoomPowerService roomPowerService;
    private final ApplianceMapper mapper;

    @Operation(summary = "Получить прибор по id (service-to-service)")
    @GetMapping("/{id}")
    public ResponseEntity<ApplianceDto> get(@PathVariable UUID id) {
        return ResponseEntity.ok(mapper.toDto(applianceService.getById(id)));
    }

    @Operation(summary = "Приборы комнаты с суммарной мощностью (service-to-service)")
    @GetMapping("/by-room/{roomId}")
    public ResponseEntity<RoomAppliancesDto> byRoom(@PathVariable UUID roomId) {
        var appliances = applianceService.listApprovedByRoom(roomId);
        int total = appliances.stream().mapToInt(Appliance::getPowerWatts).sum();
        return ResponseEntity.ok(new RoomAppliancesDto(roomId, total,
                roomPowerService.powerLimitWatts(), mapper.toDtoList(appliances)));
    }
}
