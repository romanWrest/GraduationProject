package ru.dstu.dormitory.residents_service.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.residents_service.aspect.annotation.LogMethod;
import ru.dstu.dormitory.residents_service.client.AuthServiceClient;
import ru.dstu.dormitory.residents_service.client.dto.AuthUserDto;
import ru.dstu.dormitory.residents_service.domain.enums.ResidentKind;
import ru.dstu.dormitory.residents_service.domain.model.ResidencyHistory;
import ru.dstu.dormitory.residents_service.domain.model.Resident;
import ru.dstu.dormitory.residents_service.domain.model.Room;
import ru.dstu.dormitory.residents_service.domain.repo.ResidencyHistoryRepository;
import ru.dstu.dormitory.residents_service.domain.repo.ResidentRepository;
import ru.dstu.dormitory.residents_service.domain.repo.ResidentSpecifications;
import ru.dstu.dormitory.residents_service.exception.AuthServiceUnavailableException;
import ru.dstu.dormitory.residents_service.exception.InvalidEvictionDateException;
import ru.dstu.dormitory.residents_service.exception.InvalidResidentDataException;
import ru.dstu.dormitory.residents_service.exception.ResidentAlreadyExistsException;
import ru.dstu.dormitory.residents_service.exception.ResidentNotFoundException;
import ru.dstu.dormitory.residents_service.exception.RoomIsFullException;
import ru.dstu.dormitory.residents_service.service.ResidentService;
import ru.dstu.dormitory.residents_service.service.RoomService;
import ru.dstu.dormitory.residents_service.service.event.EventPublisher;
import ru.dstu.dormitory.residents_service.service.event.EventTypes;
import ru.dstu.dormitory.residents_service.service.event.ResidentEnrolledEvent;
import ru.dstu.dormitory.residents_service.service.event.ResidentEvictedEvent;
import ru.dstu.dormitory.residents_service.service.event.ResidentMovedEvent;
import ru.dstu.dormitory.residents_service.util.LogPatterns;
import ru.dstu.dormitory.residents_service.web.dto.request.EnrollResidentRequest;
import ru.dstu.dormitory.residents_service.web.dto.request.EvictResidentRequest;
import ru.dstu.dormitory.residents_service.web.dto.request.MoveResidentRequest;
import ru.dstu.dormitory.residents_service.web.dto.request.UpdateResidentRequest;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResidentServiceImpl implements ResidentService {

    private final ResidentRepository residentRepository;
    private final ResidencyHistoryRepository historyRepository;
    private final RoomService roomService;
    private final AuthServiceClient authServiceClient;
    private final EventPublisher eventPublisher;

    @Override
    @LogMethod
    @Transactional
    public Resident enroll(EnrollResidentRequest request) {
        verifyAuthUser(request.userId());

        if (residentRepository.existsByUserIdAndEvictedAtIsNull(request.userId())) {
            throw new ResidentAlreadyExistsException(
                    "Пользователь уже проживает: userId=%s".formatted(request.userId()));
        }
        validateKindSpecificFields(request.kind(), request.faculty(), request.studyGroup(), request.department());

        Room room = roomService.getById(request.roomId());
        long occupied = roomService.countOccupied(room.getId());
        if (occupied >= room.getCapacity()) {
            throw new RoomIsFullException(
                    "Комната переполнена: id=%s, capacity=%d, occupied=%d"
                            .formatted(room.getId(), room.getCapacity(), occupied));
        }

        Resident resident = Resident.builder()
                .userId(request.userId())
                .kind(request.kind())
                .faculty(request.faculty())
                .studyGroup(request.studyGroup())
                .department(request.department())
                .phone(request.phone())
                .contactInfo(request.contactInfo())
                .room(room)
                .enrolledAt(request.enrolledAt())
                .build();
        Resident saved = residentRepository.save(resident);

        historyRepository.save(ResidencyHistory.builder()
                .resident(saved)
                .room(room)
                .movedInAt(request.enrolledAt())
                .build());

        eventPublisher.publish(
                EventTypes.AGGREGATE_RESIDENT,
                saved.getId().toString(),
                EventTypes.RESIDENT_ENROLLED,
                new ResidentEnrolledEvent(saved.getId(), saved.getUserId(), saved.getKind(),
                        room.getId(), saved.getEnrolledAt())
        );

        log.info(LogPatterns.RESIDENT_ENROLLED, saved.getId(), saved.getUserId(), room.getId(), saved.getKind());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Resident getById(UUID id) {
        return residentRepository.findById(id)
                .orElseThrow(() -> new ResidentNotFoundException("Жилец не найден: id=%s".formatted(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public Resident getByUserId(UUID userId) {
        return residentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResidentNotFoundException(
                        "Жилец не найден по userId=%s".formatted(userId)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Resident> search(ResidentKind kind, UUID roomId, String faculty,
                                 String search, Boolean active, Pageable pageable) {
        return residentRepository.findAll(
                ResidentSpecifications.search(kind, roomId, faculty, search, active),
                pageable);
    }

    @Override
    @LogMethod
    @Transactional
    public Resident update(UUID id, UpdateResidentRequest request, boolean selfOnlyFields) {
        Resident resident = getById(id);
        if (!selfOnlyFields) {
            if (request.faculty() != null) resident.setFaculty(request.faculty());
            if (request.studyGroup() != null) resident.setStudyGroup(request.studyGroup());
            if (request.department() != null) resident.setDepartment(request.department());
        }
        if (request.phone() != null) resident.setPhone(request.phone());
        if (request.contactInfo() != null) resident.setContactInfo(request.contactInfo());
        log.info(LogPatterns.RESIDENT_UPDATED, resident.getId());
        return resident;
    }

    @Override
    @LogMethod
    @Transactional
    public Resident evict(UUID id, EvictResidentRequest request) {
        Resident resident = getById(id);
        if (resident.getEvictedAt() != null) {
            throw new ResidentAlreadyExistsException(
                    "Жилец уже выселен: id=%s, evictedAt=%s".formatted(id, resident.getEvictedAt()));
        }
        if (request.evictedAt().isBefore(resident.getEnrolledAt())) {
            throw new InvalidEvictionDateException(
                    "Дата выселения (%s) раньше даты заселения (%s)"
                            .formatted(request.evictedAt(), resident.getEnrolledAt()));
        }
        resident.setEvictedAt(request.evictedAt());

        ResidencyHistory openHistory = historyRepository
                .findFirstByResidentIdAndMovedOutAtIsNullOrderByMovedInAtDesc(id)
                .orElse(null);
        if (openHistory != null) {
            openHistory.setMovedOutAt(request.evictedAt());
            openHistory.setReason(request.reason());
        }

        UUID roomId = resident.getRoom() != null ? resident.getRoom().getId() : null;
        eventPublisher.publish(
                EventTypes.AGGREGATE_RESIDENT,
                resident.getId().toString(),
                EventTypes.RESIDENT_EVICTED,
                new ResidentEvictedEvent(resident.getId(), resident.getUserId(),
                        roomId, request.evictedAt(), request.reason())
        );
        log.info(LogPatterns.RESIDENT_EVICTED, resident.getId(), resident.getEvictedAt());
        return resident;
    }

    @Override
    @LogMethod
    @Transactional
    public Resident move(UUID id, MoveResidentRequest request) {
        Resident resident = getById(id);
        if (resident.getEvictedAt() != null) {
            throw new InvalidResidentDataException(
                    "Нельзя переселить выселенного жильца: id=%s".formatted(id));
        }
        Room current = resident.getRoom();
        if (current != null && current.getId().equals(request.newRoomId())) {
            throw new InvalidResidentDataException(
                    "Жилец уже находится в комнате id=%s".formatted(request.newRoomId()));
        }
        Room newRoom = roomService.getById(request.newRoomId());
        long occupied = roomService.countOccupied(newRoom.getId());
        if (occupied >= newRoom.getCapacity()) {
            throw new RoomIsFullException(
                    "Целевая комната переполнена: id=%s, capacity=%d, occupied=%d"
                            .formatted(newRoom.getId(), newRoom.getCapacity(), occupied));
        }
        if (request.movedAt().isBefore(resident.getEnrolledAt())) {
            throw new InvalidResidentDataException(
                    "Дата переселения (%s) раньше даты заселения (%s)"
                            .formatted(request.movedAt(), resident.getEnrolledAt()));
        }

        ResidencyHistory openHistory = historyRepository
                .findFirstByResidentIdAndMovedOutAtIsNullOrderByMovedInAtDesc(id)
                .orElse(null);
        if (openHistory != null) {
            openHistory.setMovedOutAt(request.movedAt());
            openHistory.setReason(request.reason());
        }
        historyRepository.save(ResidencyHistory.builder()
                .resident(resident)
                .room(newRoom)
                .movedInAt(request.movedAt())
                .build());

        UUID fromRoomId = current != null ? current.getId() : null;
        resident.setRoom(newRoom);

        eventPublisher.publish(
                EventTypes.AGGREGATE_RESIDENT,
                resident.getId().toString(),
                EventTypes.RESIDENT_MOVED,
                new ResidentMovedEvent(resident.getId(), fromRoomId, newRoom.getId(), request.movedAt())
        );
        log.info(LogPatterns.RESIDENT_MOVED, resident.getId(), fromRoomId, newRoom.getId());
        return resident;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Resident> findActiveByRoom(UUID roomId) {
        return residentRepository.findByRoomIdAndEvictedAtIsNull(roomId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResidencyHistory> getHistoryByResident(UUID residentId) {
        if (!residentRepository.existsById(residentId)) {
            throw new ResidentNotFoundException("Жилец не найден: id=%s".formatted(residentId));
        }
        return historyRepository.findByResidentIdOrderByMovedInAtAsc(residentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResidencyHistory> getHistoryByRoom(UUID roomId) {
        roomService.getById(roomId);
        return historyRepository.findByRoomIdOrderByMovedInAtAsc(roomId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Resident> findAllByIds(List<UUID> ids) {
        return residentRepository.findAllByIdIn(ids);
    }

    private void verifyAuthUser(UUID userId) {
        try {
            AuthUserDto user = authServiceClient.getUser(userId);
            if (user == null) {
                throw new AuthServiceUnavailableException(
                        "Пользователь auth-service недоступен или не найден: userId=%s".formatted(userId));
            }
            log.info(LogPatterns.AUTH_USER_SYNCED, userId);
        } catch (AuthServiceUnavailableException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn(LogPatterns.AUTH_SERVICE_UNAVAILABLE, ex.getMessage());
            throw new AuthServiceUnavailableException(
                    "auth-service недоступен при проверке userId=%s".formatted(userId), ex);
        }
    }

    private void validateKindSpecificFields(ResidentKind kind, String faculty, String studyGroup, String department) {
        switch (kind) {
            case STUDENT -> {
                if (isBlank(faculty) || isBlank(studyGroup)) {
                    throw new InvalidResidentDataException(
                            "Для STUDENT обязательны поля faculty и studyGroup");
                }
            }
            case TEACHER -> {
                if (isBlank(department)) {
                    throw new InvalidResidentDataException("Для TEACHER обязательно поле department");
                }
            }
            case STAFF_LIVING -> {
                // дополнительных обязательных полей нет
            }
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
