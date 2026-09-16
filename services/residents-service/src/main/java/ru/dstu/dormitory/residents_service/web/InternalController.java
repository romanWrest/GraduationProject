package ru.dstu.dormitory.residents_service.web;

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
import ru.dstu.dormitory.residents_service.mapper.ResidentMapper;
import ru.dstu.dormitory.residents_service.mapper.RoomMapper;
import ru.dstu.dormitory.residents_service.service.ResidentService;
import ru.dstu.dormitory.residents_service.service.RoomService;
import ru.dstu.dormitory.residents_service.web.dto.request.BatchResidentIdsRequest;
import ru.dstu.dormitory.residents_service.web.dto.response.ResidentDto;
import ru.dstu.dormitory.residents_service.web.dto.response.RoomDto;

import java.util.List;
import java.util.UUID;

@Tag(name = "Internal", description = "Служебные эндпоинты для межсервисного взаимодействия")
@RestController
@RequestMapping("/api/v1/internal")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SERVICE', 'ADMIN')")
public class InternalController {

    private final ResidentService residentService;
    private final RoomService roomService;
    private final ResidentMapper residentMapper;
    private final RoomMapper roomMapper;

    @Operation(summary = "Получить жильца по id (service-to-service)")
    @GetMapping("/residents/{id}")
    public ResponseEntity<ResidentDto> getResident(@PathVariable UUID id) {
        return ResponseEntity.ok(residentMapper.toDto(residentService.getById(id)));
    }

    @Operation(summary = "Получить жильца по userId (service-to-service)")
    @GetMapping("/residents/by-user/{userId}")
    public ResponseEntity<ResidentDto> getByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(residentMapper.toDto(residentService.getByUserId(userId)));
    }

    @Operation(summary = "Batch-получение жильцов")
    @PostMapping("/residents/batch")
    public ResponseEntity<List<ResidentDto>> batch(@Valid @RequestBody BatchResidentIdsRequest request) {
        return ResponseEntity.ok(residentMapper.toDtoList(residentService.findAllByIds(request.ids())));
    }

    @Operation(summary = "Получить комнату по id (service-to-service)")
    @GetMapping("/rooms/{id}")
    public ResponseEntity<RoomDto> getRoom(@PathVariable UUID id) {
        var room = roomService.getById(id);
        return ResponseEntity.ok(roomMapper.toDto(room, roomService.countOccupied(room.getId())));
    }
}
