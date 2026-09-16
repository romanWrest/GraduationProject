package ru.dstu.dormitory.residents_service.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.dstu.dormitory.residents_service.domain.model.InventoryItem;
import ru.dstu.dormitory.residents_service.domain.model.ResidencyHistory;
import ru.dstu.dormitory.residents_service.domain.model.Resident;
import ru.dstu.dormitory.residents_service.domain.model.Room;
import ru.dstu.dormitory.residents_service.mapper.InventoryMapper;
import ru.dstu.dormitory.residents_service.mapper.ResidentMapper;
import ru.dstu.dormitory.residents_service.mapper.RoomMapper;
import ru.dstu.dormitory.residents_service.service.InventoryService;
import ru.dstu.dormitory.residents_service.service.ResidentService;
import ru.dstu.dormitory.residents_service.service.RoomService;
import ru.dstu.dormitory.residents_service.web.dto.request.CreateRoomRequest;
import ru.dstu.dormitory.residents_service.web.dto.request.UpdateRoomRequest;
import ru.dstu.dormitory.residents_service.web.dto.response.InventoryItemDto;
import ru.dstu.dormitory.residents_service.web.dto.response.ResidencyHistoryDto;
import ru.dstu.dormitory.residents_service.web.dto.response.ResidentDto;
import ru.dstu.dormitory.residents_service.web.dto.response.RoomDetailDto;
import ru.dstu.dormitory.residents_service.web.dto.response.RoomDto;

import java.util.List;
import java.util.UUID;

@Tag(name = "Rooms", description = "Комнаты и их наполнение")
@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final ResidentService residentService;
    private final InventoryService inventoryService;
    private final RoomMapper roomMapper;
    private final ResidentMapper residentMapper;
    private final InventoryMapper inventoryMapper;

    @Operation(summary = "Список комнат с фильтрами и пагинацией")
    @GetMapping
    public ResponseEntity<Page<RoomDto>> list(@RequestParam(required = false) Short floor,
                                              @RequestParam(required = false) Short capacity,
                                              @RequestParam(required = false) Boolean hasFreeBeds,
                                              Pageable pageable) {
        Page<Room> page = roomService.search(floor, capacity, hasFreeBeds, pageable);
        Page<RoomDto> dtoPage = page.map(r -> roomMapper.toDto(r, roomService.countOccupied(r.getId())));
        return ResponseEntity.ok(dtoPage);
    }

    @Operation(summary = "Создать комнату (ADMIN)")
    @PostMapping
    public ResponseEntity<RoomDto> create(@Valid @RequestBody CreateRoomRequest request) {
        Room room = roomService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(roomMapper.toDto(room, 0L));
    }

    @Operation(summary = "Детали комнаты: жильцы + инвентарь")
    @GetMapping("/{id}")
    public ResponseEntity<RoomDetailDto> get(@PathVariable UUID id) {
        Room room = roomService.getById(id);
        List<Resident> residents = residentService.findActiveByRoom(id);
        List<InventoryItem> inventory = inventoryService.findByRoom(id);
        long occupied = residents.size();
        List<ResidentDto> residentDtos = residentMapper.toDtoList(residents);
        List<InventoryItemDto> inventoryDtos = inventoryMapper.toDtoList(inventory);
        return ResponseEntity.ok(roomMapper.toDetail(room, occupied, residentDtos, inventoryDtos));
    }

    @Operation(summary = "Обновить комнату (ADMIN)")
    @PatchMapping("/{id}")
    public ResponseEntity<RoomDto> update(@PathVariable UUID id,
                                          @Valid @RequestBody UpdateRoomRequest request) {
        Room room = roomService.update(id, request);
        return ResponseEntity.ok(roomMapper.toDto(room, roomService.countOccupied(room.getId())));
    }

    @Operation(summary = "Удалить комнату (ADMIN). Нельзя, если есть жильцы")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        roomService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Текущие жильцы комнаты")
    @GetMapping("/{id}/residents")
    public ResponseEntity<List<ResidentDto>> residents(@PathVariable UUID id) {
        roomService.getById(id);
        return ResponseEntity.ok(residentMapper.toDtoList(residentService.findActiveByRoom(id)));
    }

    @Operation(summary = "История проживания комнаты")
    @GetMapping("/{id}/history")
    public ResponseEntity<List<ResidencyHistoryDto>> history(@PathVariable UUID id) {
        List<ResidencyHistory> history = residentService.getHistoryByRoom(id);
        return ResponseEntity.ok(residentMapper.toHistoryDtoList(history));
    }
}
