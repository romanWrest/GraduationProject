package ru.dstu.dormitory.appliances_service.service;

import ru.dstu.dormitory.appliances_service.domain.model.Appliance;

import java.util.List;
import java.util.UUID;

public interface RoomPowerService {

    /**
     * Текущая суммарная мощность по APPROVED-приборам в комнате.
     */
    int totalPowerWatts(UUID roomId);

    /**
     * Список APPROVED-приборов комнаты.
     */
    List<Appliance> approvedAppliancesInRoom(UUID roomId);

    /**
     * Конфигурируемый лимит мощности комнаты (Вт).
     */
    int powerLimitWatts();

    /**
     * Проверяет, можно ли добавить прибор в комнату с учётом лимита.
     * При превышении бросает {@code RoomPowerLimitExceededException}.
     */
    void requireWithinLimit(UUID roomId, int additionalWatts);
}
