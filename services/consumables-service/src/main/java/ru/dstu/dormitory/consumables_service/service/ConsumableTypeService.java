package ru.dstu.dormitory.consumables_service.service;

import ru.dstu.dormitory.consumables_service.domain.model.ConsumableType;
import ru.dstu.dormitory.consumables_service.web.dto.request.CreateConsumableTypeRequest;
import ru.dstu.dormitory.consumables_service.web.dto.request.UpdateConsumableTypeRequest;

import java.util.List;
import java.util.UUID;

public interface ConsumableTypeService {

    /**
     * Создать тип расходника.
     */
    ConsumableType create(CreateConsumableTypeRequest request);

    /**
     * Обновить название и/или порог низкого остатка. Stock не редактируется здесь.
     */
    ConsumableType update(UUID id, UpdateConsumableTypeRequest request);

    /**
     * Получить тип по id.
     */
    ConsumableType getById(UUID id);

    /**
     * Список всех типов.
     */
    List<ConsumableType> listAll();
}
