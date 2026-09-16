package ru.dstu.dormitory.residents_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.dstu.dormitory.residents_service.client.AuthServiceClient;
import ru.dstu.dormitory.residents_service.client.dto.AuthUserDto;
import ru.dstu.dormitory.residents_service.domain.enums.ResidentKind;
import ru.dstu.dormitory.residents_service.domain.model.ResidencyHistory;
import ru.dstu.dormitory.residents_service.domain.model.Resident;
import ru.dstu.dormitory.residents_service.domain.model.Room;
import ru.dstu.dormitory.residents_service.domain.repo.ResidencyHistoryRepository;
import ru.dstu.dormitory.residents_service.domain.repo.ResidentRepository;
import ru.dstu.dormitory.residents_service.exception.AuthServiceUnavailableException;
import ru.dstu.dormitory.residents_service.exception.InvalidResidentDataException;
import ru.dstu.dormitory.residents_service.exception.ResidentAlreadyExistsException;
import ru.dstu.dormitory.residents_service.exception.RoomIsFullException;
import ru.dstu.dormitory.residents_service.service.Impl.ResidentServiceImpl;
import ru.dstu.dormitory.residents_service.service.event.EventPublisher;
import ru.dstu.dormitory.residents_service.service.event.EventTypes;
import ru.dstu.dormitory.residents_service.web.dto.request.EnrollResidentRequest;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResidentServiceTest {

    @Mock
    private ResidentRepository residentRepository;
    @Mock
    private ResidencyHistoryRepository historyRepository;
    @Mock
    private RoomService roomService;
    @Mock
    private AuthServiceClient authServiceClient;
    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private ResidentServiceImpl service;

    private Room room;
    private UUID userId;
    private UUID roomId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        roomId = UUID.randomUUID();
        room = Room.builder().id(roomId).number("101").floor((short) 1).capacity((short) 2).build();
    }

    @Test
    @DisplayName("Успешное заселение студента: save + history + event")
    void enroll_ok() {
        when(authServiceClient.getUser(userId)).thenReturn(user());
        when(residentRepository.existsByUserIdAndEvictedAtIsNull(userId)).thenReturn(false);
        when(roomService.getById(roomId)).thenReturn(room);
        when(roomService.countOccupied(roomId)).thenReturn(0L);
        when(residentRepository.save(any(Resident.class))).thenAnswer(inv -> {
            Resident r = inv.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        EnrollResidentRequest req = new EnrollResidentRequest(
                userId, ResidentKind.STUDENT, "ФИТ", "ИТ-31", null, "+7000", null,
                roomId, LocalDate.of(2026, 9, 1));
        Resident saved = service.enroll(req);

        assertThat(saved.getKind()).isEqualTo(ResidentKind.STUDENT);
        verify(historyRepository).save(any(ResidencyHistory.class));
        verify(eventPublisher).publish(
                eq(EventTypes.AGGREGATE_RESIDENT),
                anyString(),
                eq(EventTypes.RESIDENT_ENROLLED),
                any());
    }

    @Test
    @DisplayName("Пользователя нет в auth → AuthServiceUnavailableException")
    void enroll_authMissing() {
        when(authServiceClient.getUser(userId)).thenReturn(null);

        EnrollResidentRequest req = new EnrollResidentRequest(
                userId, ResidentKind.STUDENT, "ФИТ", "ИТ-31", null, null, null,
                roomId, LocalDate.now());
        assertThatThrownBy(() -> service.enroll(req))
                .isInstanceOf(AuthServiceUnavailableException.class);
    }

    @Test
    @DisplayName("Дубль активного заселения → ResidentAlreadyExistsException")
    void enroll_duplicate() {
        when(authServiceClient.getUser(userId)).thenReturn(user());
        when(residentRepository.existsByUserIdAndEvictedAtIsNull(userId)).thenReturn(true);

        EnrollResidentRequest req = new EnrollResidentRequest(
                userId, ResidentKind.STUDENT, "ФИТ", "ИТ-31", null, null, null,
                roomId, LocalDate.now());
        assertThatThrownBy(() -> service.enroll(req))
                .isInstanceOf(ResidentAlreadyExistsException.class);
        verify(residentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Комната переполнена → RoomIsFullException")
    void enroll_roomFull() {
        when(authServiceClient.getUser(userId)).thenReturn(user());
        when(residentRepository.existsByUserIdAndEvictedAtIsNull(userId)).thenReturn(false);
        when(roomService.getById(roomId)).thenReturn(room);
        when(roomService.countOccupied(roomId)).thenReturn(2L);

        EnrollResidentRequest req = new EnrollResidentRequest(
                userId, ResidentKind.STUDENT, "ФИТ", "ИТ-31", null, null, null,
                roomId, LocalDate.now());
        assertThatThrownBy(() -> service.enroll(req))
                .isInstanceOf(RoomIsFullException.class);
    }

    @Test
    @DisplayName("STUDENT без faculty → InvalidResidentDataException")
    void enroll_studentMissingFields() {
        when(authServiceClient.getUser(userId)).thenReturn(user());
        when(residentRepository.existsByUserIdAndEvictedAtIsNull(userId)).thenReturn(false);

        EnrollResidentRequest req = new EnrollResidentRequest(
                userId, ResidentKind.STUDENT, null, null, null, null, null,
                roomId, LocalDate.now());
        assertThatThrownBy(() -> service.enroll(req))
                .isInstanceOf(InvalidResidentDataException.class);
    }

    @Test
    @DisplayName("TEACHER без department → InvalidResidentDataException")
    void enroll_teacherMissingDept() {
        when(authServiceClient.getUser(userId)).thenReturn(user());
        when(residentRepository.existsByUserIdAndEvictedAtIsNull(userId)).thenReturn(false);

        EnrollResidentRequest req = new EnrollResidentRequest(
                userId, ResidentKind.TEACHER, null, null, null, null, null,
                roomId, LocalDate.now());
        assertThatThrownBy(() -> service.enroll(req))
                .isInstanceOf(InvalidResidentDataException.class);
    }

    private AuthUserDto user() {
        return new AuthUserDto(userId, "u@example.com", "User", "+7000", true, Set.of(), null, null);
    }
}
