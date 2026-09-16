package ru.dstu.dormitory.requests_service.service;

import org.junit.jupiter.api.Test;
import ru.dstu.dormitory.requests_service.domain.enums.RequestType;
import ru.dstu.dormitory.requests_service.domain.enums.TargetPool;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoutingServiceTest {

    private final RoutingService routing = new RoutingService();

    @Test
    void mapsRepairTypesToMatchingExecutorPool() {
        assertEquals(TargetPool.EXECUTOR_ELECTRIC, routing.resolvePool(RequestType.REPAIR_ELECTRIC));
        assertEquals(TargetPool.EXECUTOR_PLUMBING, routing.resolvePool(RequestType.REPAIR_PLUMBING));
        assertEquals(TargetPool.EXECUTOR_CARPENTRY, routing.resolvePool(RequestType.REPAIR_CARPENTRY));
        assertEquals(TargetPool.EXECUTOR_GAS, routing.resolvePool(RequestType.REPAIR_GAS));
    }

    @Test
    void mapsLinenToPropertyManager() {
        assertEquals(TargetPool.PROPERTY_MANAGER, routing.resolvePool(RequestType.LINEN_REPLACEMENT));
    }

    @Test
    void mapsAdminTypesToAdmin() {
        assertEquals(TargetPool.ADMIN, routing.resolvePool(RequestType.GUEST_PASS));
        assertEquals(TargetPool.ADMIN, routing.resolvePool(RequestType.ITEM_MOVEMENT));
        assertEquals(TargetPool.ADMIN, routing.resolvePool(RequestType.RELOCATION));
        assertEquals(TargetPool.ADMIN, routing.resolvePool(RequestType.COMPLAINT));
        assertEquals(TargetPool.ADMIN, routing.resolvePool(RequestType.ELECTRICAL_APPLIANCE));
        assertEquals(TargetPool.ADMIN, routing.resolvePool(RequestType.OTHER));
    }
}
