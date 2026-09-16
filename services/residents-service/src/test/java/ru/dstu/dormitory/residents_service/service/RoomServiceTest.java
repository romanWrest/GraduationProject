package ru.dstu.dormitory.residents_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.dstu.dormitory.residents_service.domain.model.Room;
import ru.dstu.dormitory.residents_service.domain.repo.ResidentRepository;
import ru.dstu.dormitory.residents_service.domain.repo.RoomRepository;
import ru.dstu.dormitory.residents_service.exception.RoomHasResidentsException;
import ru.dstu.dormitory.residents_service.exception.RoomNotFoundException;
import ru.dstu.dormitory.residents_service.service.Impl.RoomServiceImpl;
import ru.dstu.dormitory.residents_service.web.dto.request.CreateRoomRequest;
import ru.dstu.dormitory.residents_service.web.dto.request.UpdateRoomRequest;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private ResidentRepository residentRepository;

    @InjectMocks
    private RoomServiceImpl service;

    private Room room;
    private UUID roomId;

    @BeforeEach
    void setUp() {
        roomId = UUID.randomUUID();
        room = Room.builder()
                .id(roomId)
                .number("101")
                .floor((short) 1)
                .capacity((short) 3)
                .build();
    }

    @Test
    @DisplayName("Создание комнаты сохраняет её и возвращает Room")
    void create_ok() {
        when(roomRepository.existsByNumber("101")).thenReturn(false);
        when(roomRepository.save(any(Room.class))).thenAnswer(inv -> inv.getArgument(0));

        Room created = service.create(new CreateRoomRequest("101", (short) 1, (short) 3, "Чисто"));

        assertThat(created.getNumber()).isEqualTo("101");
        verify(roomRepository).save(any(Room.class));
    }

    @Test
    @DisplayName("Создание с занятым номером → IllegalArgumentException")
    void create_duplicateNumber() {
        when(roomRepository.existsByNumber("101")).thenReturn(true);
        assertThatThrownBy(() -> service.create(
                new CreateRoomRequest("101", (short) 1, (short) 3, null)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(roomRepository, never()).save(any());
    }

    @Test
    @DisplayName("getById бросает RoomNotFound")
    void getById_notFound() {
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(roomId))
                .isInstanceOf(RoomNotFoundException.class);
    }

    @Test
    @DisplayName("Удаление комнаты с активными жильцами → RoomHasResidentsException")
    void delete_withResidents() {
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(residentRepository.countActiveByRoomId(roomId)).thenReturn(2L);

        assertThatThrownBy(() -> service.delete(roomId))
                .isInstanceOf(RoomHasResidentsException.class);
        verify(roomRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Удаление пустой комнаты успешно")
    void delete_ok() {
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(residentRepository.countActiveByRoomId(roomId)).thenReturn(0L);

        service.delete(roomId);

        verify(roomRepository).delete(room);
    }

    @Test
    @DisplayName("Уменьшение capacity ниже числа жильцов → IllegalArgumentException")
    void update_capacityTooSmall() {
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(residentRepository.countActiveByRoomId(roomId)).thenReturn(2L);

        UpdateRoomRequest req = new UpdateRoomRequest(null, null, (short) 1, null);
        assertThatThrownBy(() -> service.update(roomId, req))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
