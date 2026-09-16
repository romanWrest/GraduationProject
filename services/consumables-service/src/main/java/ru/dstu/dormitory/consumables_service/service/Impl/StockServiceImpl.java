package ru.dstu.dormitory.consumables_service.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.consumables_service.aspect.annotation.LogMethod;
import ru.dstu.dormitory.consumables_service.domain.model.ConsumableType;
import ru.dstu.dormitory.consumables_service.domain.model.StockMovement;
import ru.dstu.dormitory.consumables_service.domain.repo.ConsumableTypeRepository;
import ru.dstu.dormitory.consumables_service.domain.repo.StockMovementRepository;
import ru.dstu.dormitory.consumables_service.exception.ConsumableTypeNotFoundException;
import ru.dstu.dormitory.consumables_service.exception.InvalidStockOperationException;
import ru.dstu.dormitory.consumables_service.exception.OutOfStockException;
import ru.dstu.dormitory.consumables_service.service.StockService;
import ru.dstu.dormitory.consumables_service.service.event.ConsumableStockLowEvent;
import ru.dstu.dormitory.consumables_service.service.event.ConsumableStockOutEvent;
import ru.dstu.dormitory.consumables_service.service.event.EventPublisher;
import ru.dstu.dormitory.consumables_service.service.event.EventTypes;
import ru.dstu.dormitory.consumables_service.util.LogPatterns;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final ConsumableTypeRepository typeRepository;
    private final StockMovementRepository movementRepository;
    private final EventPublisher eventPublisher;

    @Override
    @LogMethod(logArgs = {"typeId", "delta", "reason"})
    @Transactional
    public ConsumableType adjustStock(UUID typeId, int delta, String reason, UUID actorId) {
        if (delta == 0) {
            throw new InvalidStockOperationException("Дельта остатка не может быть 0");
        }
        ConsumableType type = typeRepository.findByIdForUpdate(typeId)
                .orElseThrow(() -> new ConsumableTypeNotFoundException(
                        "Тип расходника не найден: id=" + typeId));

        int oldStock = type.getStock();
        int newStock = oldStock + delta;
        if (newStock < 0) {
            throw new OutOfStockException(
                    "Недостаточно остатка: typeId=%s, текущий=%d, требуется=%d"
                            .formatted(typeId, oldStock, -delta));
        }

        type.setStock(newStock);
        ConsumableType saved = typeRepository.save(type);

        movementRepository.save(StockMovement.builder()
                .type(saved)
                .delta(delta)
                .reason(reason)
                .actorId(actorId)
                .build());

        log.info(LogPatterns.CONSUMABLE_STOCK_UPDATED, typeId, oldStock, newStock, reason);

        publishStockAlertsIfNeeded(saved, oldStock);
        return saved;
    }

    private void publishStockAlertsIfNeeded(ConsumableType type, int oldStock) {
        int newStock = type.getStock();
        int threshold = type.getLowStockThreshold();

        if (newStock == 0 && oldStock > 0) {
            log.warn(LogPatterns.STOCK_OUT, type.getId(), type.getName());
            eventPublisher.publish(EventTypes.AGGREGATE_TYPE, type.getId().toString(),
                    EventTypes.CONSUMABLE_STOCK_OUT,
                    new ConsumableStockOutEvent(type.getId(), type.getName()));
        } else if (newStock > 0 && newStock <= threshold && oldStock > threshold) {
            log.warn(LogPatterns.STOCK_LOW, type.getId(), type.getName(), newStock);
            eventPublisher.publish(EventTypes.AGGREGATE_TYPE, type.getId().toString(),
                    EventTypes.CONSUMABLE_STOCK_LOW,
                    new ConsumableStockLowEvent(type.getId(), type.getName(), newStock, threshold));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsumableType> listAll() {
        return typeRepository.findAll();
    }
}
