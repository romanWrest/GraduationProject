package ru.dstu.dormitory.consumables_service.service;

import ru.dstu.dormitory.consumables_service.domain.model.ConsumableType;

import java.util.List;
import java.util.UUID;

public interface StockService {

    /**
     * Применяет дельту к остатку расходника с pessimistic lock на строку.
     * Положительная — пополнение, отрицательная — списание.
     * При попытке уйти ниже нуля выбрасывает {@code OutOfStockException}.
     * Также фиксирует движение в stock_movement и при необходимости публикует
     * {@code ConsumableStockLow}/{@code ConsumableStockOut}.
     *
     * @return обновлённый тип расходника
     */
    ConsumableType adjustStock(UUID typeId, int delta, String reason, UUID actorId);

    /**
     * Получить полный список текущих остатков.
     */
    List<ConsumableType> listAll();
}
