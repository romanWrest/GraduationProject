package ru.dstu.dormitory.residents_service.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.dstu.dormitory.residents_service.domain.model.InventoryItem;
import ru.dstu.dormitory.residents_service.mapper.InventoryMapper;
import ru.dstu.dormitory.residents_service.service.InventoryService;
import ru.dstu.dormitory.residents_service.web.dto.request.CreateInventoryRequest;
import ru.dstu.dormitory.residents_service.web.dto.request.UpdateInventoryRequest;
import ru.dstu.dormitory.residents_service.web.dto.request.WriteOffInventoryRequest;
import ru.dstu.dormitory.residents_service.web.dto.response.InventoryItemDto;

import java.util.List;
import java.util.UUID;

@Tag(name = "Inventory", description = "Инвентарь комнат")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final InventoryMapper inventoryMapper;

    @Operation(summary = "Инвентарь комнаты")
    @GetMapping("/rooms/{roomId}/inventory")
    public ResponseEntity<List<InventoryItemDto>> byRoom(@PathVariable UUID roomId) {
        List<InventoryItem> items = inventoryService.findByRoom(roomId);
        return ResponseEntity.ok(inventoryMapper.toDtoList(items));
    }

    @Operation(summary = "Добавить позицию инвентаря в комнату (ADMIN)")
    @PostMapping("/rooms/{roomId}/inventory")
    public ResponseEntity<InventoryItemDto> add(@PathVariable UUID roomId,
                                                @Valid @RequestBody CreateInventoryRequest request) {
        InventoryItem item = inventoryService.add(roomId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryMapper.toDto(item));
    }

    @Operation(summary = "Получить позицию инвентаря")
    @GetMapping("/inventory/{id}")
    public ResponseEntity<InventoryItemDto> get(@PathVariable UUID id) {
        return ResponseEntity.ok(inventoryMapper.toDto(inventoryService.getById(id)));
    }

    @Operation(summary = "Изменить позицию инвентаря (ADMIN)")
    @PatchMapping("/inventory/{id}")
    public ResponseEntity<InventoryItemDto> update(@PathVariable UUID id,
                                                   @Valid @RequestBody UpdateInventoryRequest request) {
        return ResponseEntity.ok(inventoryMapper.toDto(inventoryService.update(id, request)));
    }

    @Operation(summary = "Списать позицию инвентаря (ADMIN)")
    @PostMapping("/inventory/{id}/write-off")
    public ResponseEntity<InventoryItemDto> writeOff(@PathVariable UUID id,
                                                     @Valid @RequestBody WriteOffInventoryRequest request) {
        return ResponseEntity.ok(inventoryMapper.toDto(inventoryService.writeOff(id, request)));
    }

    @Operation(summary = "Удалить позицию. Только если state=WRITTEN_OFF")
    @DeleteMapping("/inventory/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        inventoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
