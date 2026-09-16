package ru.dstu.dormitory.appliances_service.domain.statemachine;

import org.springframework.stereotype.Component;
import ru.dstu.dormitory.appliances_service.domain.enums.ApplianceStatus;
import ru.dstu.dormitory.appliances_service.exception.IllegalApplianceTransitionException;

import java.util.Map;
import java.util.Set;

/**
 * Машина состояний жизненного цикла прибора.
 * <pre>
 *   PENDING → APPROVED → REVOKED
 *          ↘ REJECTED
 * </pre>
 */
@Component
public class ApplianceStateMachine {

    private static final Map<ApplianceStatus, Set<ApplianceStatus>> ALLOWED = Map.of(
            ApplianceStatus.PENDING, Set.of(ApplianceStatus.APPROVED, ApplianceStatus.REJECTED),
            ApplianceStatus.APPROVED, Set.of(ApplianceStatus.REVOKED),
            ApplianceStatus.REJECTED, Set.of(),
            ApplianceStatus.REVOKED, Set.of()
    );

    public boolean canTransition(ApplianceStatus from, ApplianceStatus to) {
        if (from == null || to == null) {
            return false;
        }
        return ALLOWED.getOrDefault(from, Set.of()).contains(to);
    }

    public void requireTransition(ApplianceStatus from, ApplianceStatus to) {
        if (!canTransition(from, to)) {
            throw new IllegalApplianceTransitionException(
                    "Недопустимый переход %s → %s".formatted(from, to));
        }
    }
}
