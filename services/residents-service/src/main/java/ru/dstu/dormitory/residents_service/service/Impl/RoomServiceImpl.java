package ru.dstu.dormitory.residents_service.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.residents_service.aspect.annotation.LogMethod;
import ru.dstu.dormitory.residents_service.domain.model.Room;
import ru.dstu.dormitory.residents_service.domain.repo.ResidentRepository;
import ru.dstu.dormitory.residents_service.domain.repo.RoomRepository;
import ru.dstu.dormitory.residents_service.exception.RoomHasResidentsException;
import ru.dstu.dormitory.residents_service.exception.RoomNotFoundException;
import ru.dstu.dormitory.residents_service.service.RoomService;
import ru.dstu.dormitory.residents_service.util.LogPatterns;
import ru.dstu.dormitory.residents_service.web.dto.request.CreateRoomRequest;
import ru.dstu.dormitory.residents_service.web.dto.request.UpdateRoomRequest;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final ResidentRepository residentRepository;

    @Override
    @LogMethod
    @Transactional
    public Room create(CreateRoomRequest request) {
        if (roomRepository.existsByNumber(request.number())) {
            throw new IllegalArgumentException("Комната с номером '%s' уже существует".formatted(request.number()));
        }
        Room room = Room.builder()
                .number(request.number())
                .floor(request.floor())
                .capacity(request.capacity())
                .notes(request.notes())
                .build();
        Room saved = roomRepository.save(room);
        log.info(LogPatterns.ROOM_CREATED, saved.getId(), saved.getNumber(), saved.getFloor());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Room getById(UUID id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException("Комната не найдена: id=%s".formatted(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Room> search(Short floor, Short capacity, Boolean hasFreeBeds, Pageable pageable) {
        return roomRepository.search(floor, capacity, hasFreeBeds, pageable);
    }

    @Override
    @LogMethod
    @Transactional
    public Room update(UUID id, UpdateRoomRequest request) {
        Room room = getById(id);
        if (request.number() != null && !request.number().equals(room.getNumber())) {
            if (roomRepository.existsByNumber(request.number())) {
                throw new IllegalArgumentException(
                        "Комната с номером '%s' уже существует".formatted(request.number()));
            }
            room.setNumber(request.number());
        }
        if (request.floor() != null) {
            room.setFloor(request.floor());
        }
        if (request.capacity() != null) {
            long occupied = residentRepository.countActiveByRoomId(id);
            if (request.capacity() < occupied) {
                throw new IllegalArgumentException(
                        "Вместимость не может быть меньше текущего количества жильцов (%d)".formatted(occupied));
            }
            room.setCapacity(request.capacity());
        }
        if (request.notes() != null) {
            room.setNotes(request.notes());
        }
        log.info(LogPatterns.ROOM_UPDATED, room.getId());
        return room;
    }

    @Override
    @LogMethod
    @Transactional
    public void delete(UUID id) {
        Room room = getById(id);
        long occupied = residentRepository.countActiveByRoomId(id);
        if (occupied > 0) {
            throw new RoomHasResidentsException(
                    "Нельзя удалить комнату с активными жильцами (count=%d)".formatted(occupied));
        }
        roomRepository.delete(room);
        log.info(LogPatterns.ROOM_DELETED, id);
    }

    @Override
    @Transactional(readOnly = true)
    public long countOccupied(UUID roomId) {
        return residentRepository.countActiveByRoomId(roomId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Room> findAllByIds(List<UUID> ids) {
        return roomRepository.findAllById(ids);
    }
}
