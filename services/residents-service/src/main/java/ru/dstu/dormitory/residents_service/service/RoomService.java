package ru.dstu.dormitory.residents_service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.dstu.dormitory.residents_service.domain.model.Room;
import ru.dstu.dormitory.residents_service.web.dto.request.CreateRoomRequest;
import ru.dstu.dormitory.residents_service.web.dto.request.UpdateRoomRequest;

import java.util.List;
import java.util.UUID;

public interface RoomService {

    Room create(CreateRoomRequest request);

    Room getById(UUID id);

    Page<Room> search(Short floor, Short capacity, Boolean hasFreeBeds, Pageable pageable);

    Room update(UUID id, UpdateRoomRequest request);

    void delete(UUID id);

    /**
     * Количество проживающих в комнате (evicted_at IS NULL).
     */
    long countOccupied(UUID roomId);

    /**
     * Получить несколько комнат по id одним запросом.
     */
    List<Room> findAllByIds(List<UUID> ids);
}
