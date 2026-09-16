package ru.dstu.dormitory.requests_service.service;

import org.springframework.stereotype.Service;
import ru.dstu.dormitory.requests_service.domain.enums.RequestType;
import ru.dstu.dormitory.requests_service.domain.enums.TargetPool;

import java.util.EnumMap;
import java.util.Map;

/**
 * Определяет целевой пул исполнителей по типу заявки.
 */
@Service
public class RoutingService {

    private static final Map<RequestType, TargetPool> ROUTING = new EnumMap<>(RequestType.class);

    static {
        ROUTING.put(RequestType.REPAIR_ELECTRIC, TargetPool.EXECUTOR_ELECTRIC);
        ROUTING.put(RequestType.REPAIR_PLUMBING, TargetPool.EXECUTOR_PLUMBING);
        ROUTING.put(RequestType.REPAIR_CARPENTRY, TargetPool.EXECUTOR_CARPENTRY);
        ROUTING.put(RequestType.REPAIR_GAS, TargetPool.EXECUTOR_GAS);
        ROUTING.put(RequestType.LINEN_REPLACEMENT, TargetPool.PROPERTY_MANAGER);
        ROUTING.put(RequestType.GUEST_PASS, TargetPool.ADMIN);
        ROUTING.put(RequestType.ITEM_MOVEMENT, TargetPool.ADMIN);
        ROUTING.put(RequestType.RELOCATION, TargetPool.ADMIN);
        ROUTING.put(RequestType.COMPLAINT, TargetPool.ADMIN);
        ROUTING.put(RequestType.ELECTRICAL_APPLIANCE, TargetPool.ADMIN);
        ROUTING.put(RequestType.OTHER, TargetPool.ADMIN);
    }

    /**
     * Возвращает пул, в который должна попасть заявка указанного типа.
     */
    public TargetPool resolvePool(RequestType type) {
        TargetPool pool = ROUTING.get(type);
        if (pool == null) {
            return TargetPool.ADMIN;
        }
        return pool;
    }
}
