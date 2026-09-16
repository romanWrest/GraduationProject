package ru.dstu.dormitory.residents_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.dstu.dormitory.residents_service.domain.enums.InventoryState;
import ru.dstu.dormitory.residents_service.domain.enums.InventoryType;
import ru.dstu.dormitory.residents_service.domain.model.InventoryItem;
import ru.dstu.dormitory.residents_service.domain.model.Room;
import ru.dstu.dormitory.residents_service.domain.repo.InventoryItemRepository;
import ru.dstu.dormitory.residents_service.exception.InvalidResidentDataException;
import ru.dstu.dormitory.residents_service.exception.InventoryItemNotFoundException;
import ru.dstu.dormitory.residents_service.service.Impl.InventoryServiceImpl;
import ru.dstu.dormitory.residents_service.web.dto.request.UpdateInventoryRequest;
import ru.dstu.dormitory.residents_service.web.dto.request.WriteOffInventoryRequest;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryItemRepository inventoryRepository;
    @Mock
    private RoomService roomService;

    @InjectMocks
    private InventoryServiceImpl service;

    private InventoryItem item;
    private UUID id;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        Room room = Room.builder().id(UUID.randomUUID()).number("101").capacity((short) 2).floor((short) 1).build();
        item = InventoryItem.builder().id(id).room(room)
                .type(InventoryType.BED).state(InventoryState.USED).build();
    }

    @Test
    @DisplayName("getById → 404 если не найден")
    void getById_notFound() {
        when(inventoryRepository.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(InventoryItemNotFoundException.class);
    }

    @Test
    @DisplayName("delete не списанного инвентаря → InvalidResidentDataException")
    void delete_notWrittenOff() {
        when(inventoryRepository.findById(id)).thenReturn(Optional.of(item));
        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(InvalidResidentDataException.class);
        verify(inventoryRepository, never()).delete(any());
    }

    @Test
    @DisplayName("delete списанного инвентаря — успешно")
    void delete_writtenOff() {
        item.setState(InventoryState.WRITTEN_OFF);
        when(inventoryRepository.findById(id)).thenReturn(Optional.of(item));
        service.delete(id);
        verify(inventoryRepository).delete(item);
    }

    @Test
    @DisplayName("writeOff уже списанной позиции → InvalidResidentDataException")
    void writeOff_alreadyWrittenOff() {
        item.setState(InventoryState.WRITTEN_OFF);
        when(inventoryRepository.findById(id)).thenReturn(Optional.of(item));
        assertThatThrownBy(() -> service.writeOff(id, new WriteOffInventoryRequest("повторно")))
                .isInstanceOf(InvalidResidentDataException.class);
    }

    @Test
    @DisplayName("writeOff проставляет состояние и причину")
    void writeOff_ok() {
        when(inventoryRepository.findById(id)).thenReturn(Optional.of(item));
        InventoryItem result = service.writeOff(id, new WriteOffInventoryRequest("сломано"));
        assertThat(result.getState()).isEqualTo(InventoryState.WRITTEN_OFF);
        assertThat(result.getWrittenOffReason()).isEqualTo("сломано");
        assertThat(result.getWrittenOffAt()).isNotNull();
    }

    @Test
    @DisplayName("update меняет состояние USED → BROKEN")
    void update_state() {
        when(inventoryRepository.findById(id)).thenReturn(Optional.of(item));
        InventoryItem result = service.update(id, new UpdateInventoryRequest(InventoryState.BROKEN, null, "царапина"));
        assertThat(result.getState()).isEqualTo(InventoryState.BROKEN);
        assertThat(result.getNotes()).isEqualTo("царапина");
    }

    @Test
    @DisplayName("update списанного инвентаря с новым state → InvalidResidentDataException")
    void update_writtenOffCannotBeUpdated() {
        item.setState(InventoryState.WRITTEN_OFF);
        when(inventoryRepository.findById(id)).thenReturn(Optional.of(item));
        assertThatThrownBy(() -> service.update(id,
                new UpdateInventoryRequest(InventoryState.USED, null, null)))
                .isInstanceOf(InvalidResidentDataException.class);
    }
}
