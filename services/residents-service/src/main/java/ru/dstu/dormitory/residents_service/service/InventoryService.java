package ru.dstu.dormitory.residents_service.service;

import ru.dstu.dormitory.residents_service.domain.model.InventoryItem;
import ru.dstu.dormitory.residents_service.web.dto.request.CreateInventoryRequest;
import ru.dstu.dormitory.residents_service.web.dto.request.UpdateInventoryRequest;
import ru.dstu.dormitory.residents_service.web.dto.request.WriteOffInventoryRequest;

import java.util.List;
import java.util.UUID;

public interface InventoryService {

    InventoryItem add(UUID roomId, CreateInventoryRequest request);

    InventoryItem getById(UUID id);

    List<InventoryItem> findByRoom(UUID roomId);

    InventoryItem update(UUID id, UpdateInventoryRequest request);

    InventoryItem writeOff(UUID id, WriteOffInventoryRequest request);

    void delete(UUID id);
}
