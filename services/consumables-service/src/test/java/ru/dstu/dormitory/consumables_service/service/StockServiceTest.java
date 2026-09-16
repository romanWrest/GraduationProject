package ru.dstu.dormitory.consumables_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.dstu.dormitory.consumables_service.domain.enums.ConsumableUnit;
import ru.dstu.dormitory.consumables_service.domain.model.ConsumableType;
import ru.dstu.dormitory.consumables_service.domain.model.StockMovement;
import ru.dstu.dormitory.consumables_service.domain.repo.ConsumableTypeRepository;
import ru.dstu.dormitory.consumables_service.domain.repo.StockMovementRepository;
import ru.dstu.dormitory.consumables_service.exception.ConsumableTypeNotFoundException;
import ru.dstu.dormitory.consumables_service.exception.InvalidStockOperationException;
import ru.dstu.dormitory.consumables_service.exception.OutOfStockException;
import ru.dstu.dormitory.consumables_service.service.Impl.StockServiceImpl;
import ru.dstu.dormitory.consumables_service.service.event.EventPublisher;
import ru.dstu.dormitory.consumables_service.service.event.EventTypes;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StockServiceTest {

    @Mock
    private ConsumableTypeRepository typeRepository;
    @Mock
    private StockMovementRepository movementRepository;
    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private StockServiceImpl service;

    private ConsumableType type;
    private UUID typeId;
    private UUID actorId;

    @BeforeEach
    void setUp() {
        typeId = UUID.randomUUID();
        actorId = UUID.randomUUID();
        type = ConsumableType.builder()
                .id(typeId)
                .name("Комплект постельного белья")
                .unit(ConsumableUnit.SET)
                .stock(20)
                .lowStockThreshold(5)
                .build();
        when(typeRepository.findByIdForUpdate(typeId)).thenReturn(Optional.of(type));
        when(typeRepository.save(any(ConsumableType.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    @DisplayName("Пополнение увеличивает остаток и сохраняет движение")
    void replenishIncreasesStock() {
        ConsumableType result = service.adjustStock(typeId, 10, "Пополнение", actorId);

        assertThat(result.getStock()).isEqualTo(30);
        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
        verify(movementRepository).save(captor.capture());
        assertThat(captor.getValue().getDelta()).isEqualTo(10);
        assertThat(captor.getValue().getReason()).isEqualTo("Пополнение");
    }

    @Test
    @DisplayName("Списание уменьшает остаток")
    void writeOffDecreasesStock() {
        ConsumableType result = service.adjustStock(typeId, -5, "Выдача", actorId);

        assertThat(result.getStock()).isEqualTo(15);
    }

    @Test
    @DisplayName("Выход остатка ниже нуля → OutOfStockException")
    void belowZeroThrows() {
        assertThatThrownBy(() -> service.adjustStock(typeId, -100, "Выдача", actorId))
                .isInstanceOf(OutOfStockException.class);
        verify(movementRepository, never()).save(any());
    }

    @Test
    @DisplayName("delta=0 → InvalidStockOperationException")
    void zeroDeltaThrows() {
        assertThatThrownBy(() -> service.adjustStock(typeId, 0, "noop", actorId))
                .isInstanceOf(InvalidStockOperationException.class);
    }

    @Test
    @DisplayName("Тип не найден → ConsumableTypeNotFoundException")
    void typeNotFound() {
        UUID missing = UUID.randomUUID();
        when(typeRepository.findByIdForUpdate(missing)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.adjustStock(missing, 1, "x", actorId))
                .isInstanceOf(ConsumableTypeNotFoundException.class);
    }

    @Test
    @DisplayName("Падение ниже threshold публикует ConsumableStockLow")
    void lowStockEventPublished() {
        type.setStock(10);
        service.adjustStock(typeId, -6, "Выдача", actorId);

        verify(eventPublisher).publish(eq(EventTypes.AGGREGATE_TYPE), eq(typeId.toString()),
                eq(EventTypes.CONSUMABLE_STOCK_LOW), any());
    }

    @Test
    @DisplayName("Снижение до 0 публикует ConsumableStockOut")
    void outOfStockEventPublished() {
        type.setStock(3);
        service.adjustStock(typeId, -3, "Выдача", actorId);

        verify(eventPublisher).publish(eq(EventTypes.AGGREGATE_TYPE), eq(typeId.toString()),
                eq(EventTypes.CONSUMABLE_STOCK_OUT), any());
    }

    @Test
    @DisplayName("Stock в норме — алерт не публикуется")
    void noAlertWhenAboveThreshold() {
        service.adjustStock(typeId, -1, "Выдача", actorId);

        verify(eventPublisher, never()).publish(anyString(), anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Stock уже был ниже threshold до операции — повторно не публикуется ConsumableStockLow")
    void noDuplicateLowAlert() {
        type.setStock(4);
        service.adjustStock(typeId, -1, "Выдача", actorId);

        verify(eventPublisher, never()).publish(anyString(), anyString(),
                eq(EventTypes.CONSUMABLE_STOCK_LOW), any());
    }
}
