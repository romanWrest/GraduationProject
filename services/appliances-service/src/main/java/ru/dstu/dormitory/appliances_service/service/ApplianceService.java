package ru.dstu.dormitory.appliances_service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.dstu.dormitory.appliances_service.domain.enums.ApplianceStatus;
import ru.dstu.dormitory.appliances_service.domain.enums.ApplianceType;
import ru.dstu.dormitory.appliances_service.domain.model.Appliance;
import ru.dstu.dormitory.appliances_service.web.dto.request.RegisterApplianceRequest;

import java.util.List;
import java.util.UUID;

public interface ApplianceService {

    /**
     * Регистрация прибора жильцом. Проверяет жильца через residents-service,
     * запоминает текущую комнату и создаёт запись со статусом PENDING.
     */
    Appliance register(UUID userId, RegisterApplianceRequest request);

    /**
     * Одобрение прибора администратором. Проверяет лимит мощности комнаты.
     */
    Appliance approve(UUID id, UUID actorId, String comment);

    /**
     * Отклонение прибора администратором.
     */
    Appliance reject(UUID id, UUID actorId, String reason);

    /**
     * Снятие прибора с учёта администратором.
     */
    Appliance revoke(UUID id, UUID actorId, String reason);

    /**
     * Автоматическое снятие всех APPROVED-приборов выселяемого жильца.
     * @return количество обработанных приборов
     */
    int autoRevokeForResident(UUID residentId, UUID userId, String reason);

    Appliance getById(UUID id);

    Page<Appliance> search(ApplianceStatus status, UUID residentId, UUID userId,
                           UUID roomId, ApplianceType type, String search, Pageable pageable);

    List<Appliance> listApprovedByRoom(UUID roomId);
}
