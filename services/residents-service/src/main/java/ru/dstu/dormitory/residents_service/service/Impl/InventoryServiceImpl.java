package ru.dstu.dormitory.residents_service.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.residents_service.aspect.annotation.LogMethod;
import ru.dstu.dormitory.residents_service.domain.enums.InventoryState;
import ru.dstu.dormitory.residents_service.domain.model.InventoryItem;
import ru.dstu.dormitory.residents_service.domain.model.Room;
import ru.dstu.dormitory.residents_service.domain.repo.InventoryItemRepository;
import ru.dstu.dormitory.residents_service.exception.InventoryItemNotFoundException;
import ru.dstu.dormitory.residents_service.exception.InvalidResidentDataException;
import ru.dstu.dormitory.residents_service.service.InventoryService;
import ru.dstu.dormitory.residents_service.service.RoomService;
import ru.dstu.dormitory.residents_service.util.LogPatterns;
import ru.dstu.dormitory.residents_service.web.dto.request.CreateInventoryRequest;
import ru.dstu.dormitory.residents_service.web.dto.request.UpdateInventoryRequest;
import ru.dstu.dormitory.residents_service.web.dto.request.WriteOffInventoryRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryItemRepository inventoryRepository;
    private final RoomService roomService;

    @Override
    @LogMethod
    @Transactional
    public InventoryItem add(UUID roomId, CreateInventoryRequest request) {
        Room room = roomService.getById(roomId);
        InventoryItem item = InventoryItem.builder()
                .room(room)
                .type(request.type())
                .serialNumber(request.serialNumber())
                .state(request.state())
                .notes(request.notes())
                .build();
        InventoryItem saved = inventoryRepository.save(item);
        log.info(LogPatterns.INVENTORY_ADDED, saved.getId(), roomId, saved.getType());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryItem getById(UUID id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new InventoryItemNotFoundException(
                        "Инвентарь не найден: id=%s".formatted(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItem> findByRoom(UUID roomId) {
        roomService.getById(roomId);
        return inventoryRepository.findByRoomIdOrderByCreatedAtAsc(roomId);
    }

    @Override
    @LogMethod
    @Transactional
    public InventoryItem update(UUID id, UpdateInventoryRequest request) {
        InventoryItem item = getById(id);
        if (request.serialNumber() != null) item.setSerialNumber(request.serialNumber());
        if (request.notes() != null) item.setNotes(request.notes());
        if (request.state() != null && request.state() != item.getState()) {
            if (item.getState() == InventoryState.WRITTEN_OFF) {
                throw new InvalidResidentDataException(
                        "Нельзя изменить состояние списанного инвентаря: id=%s".formatted(id));
            }
            InventoryState oldState = item.getState();
            item.setState(request.state());
            log.info(LogPatterns.INVENTORY_STATE_CHANGED, item.getId(), oldState, request.state());
        }
        return item;
    }

    @Override
    @LogMethod
    @Transactional
    public InventoryItem writeOff(UUID id, WriteOffInventoryRequest request) {
        InventoryItem item = getById(id);
        if (item.getState() == InventoryState.WRITTEN_OFF) {
            throw new InvalidResidentDataException(
                    "Инвентарь уже списан: id=%s".formatted(id));
        }
        InventoryState oldState = item.getState();
        item.setState(InventoryState.WRITTEN_OFF);
        item.setWrittenOffAt(Instant.now());
        item.setWrittenOffReason(request.reason());
        log.info(LogPatterns.INVENTORY_STATE_CHANGED, item.getId(), oldState, InventoryState.WRITTEN_OFF);
        log.info(LogPatterns.INVENTORY_WRITTEN_OFF, item.getId(), request.reason());
        return item;
    }

    @Override
    @LogMethod
    @Transactional
    public void delete(UUID id) {
        InventoryItem item = getById(id);
        if (item.getState() != InventoryState.WRITTEN_OFF) {
            throw new InvalidResidentDataException(
                    "Удалять можно только списанный инвентарь (state=WRITTEN_OFF), текущее state=%s"
                            .formatted(item.getState()));
        }
        inventoryRepository.delete(item);
    }
}
