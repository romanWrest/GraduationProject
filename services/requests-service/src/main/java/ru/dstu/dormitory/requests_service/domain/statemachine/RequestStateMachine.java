package ru.dstu.dormitory.requests_service.domain.statemachine;

import org.springframework.stereotype.Component;
import ru.dstu.dormitory.requests_service.domain.enums.RequestStatus;
import ru.dstu.dormitory.requests_service.domain.enums.RequestType;
import ru.dstu.dormitory.requests_service.exception.IllegalStatusTransitionException;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Валидация переходов статусов заявки.
 *
 * <p>Допустимые переходы:
 * <pre>
 * NEW → IN_REVIEW | CANCELLED
 * IN_REVIEW → ASSIGNED | REJECTED | CANCELLED
 * ASSIGNED → IN_PROGRESS | REJECTED | ASSIGNED (переназначение)
 * IN_PROGRESS → DONE
 * DONE → CLOSED
 * CLOSED → ASSIGNED (REOPEN автором в течение 7 дней)
 * </pre>
 */
@Component
public class RequestStateMachine {

    private static final Map<RequestStatus, Set<RequestStatus>> ALLOWED = new EnumMap<>(RequestStatus.class);

    static {
        ALLOWED.put(RequestStatus.NEW, EnumSet.of(RequestStatus.IN_REVIEW, RequestStatus.CANCELLED));
        ALLOWED.put(RequestStatus.IN_REVIEW, EnumSet.of(RequestStatus.ASSIGNED, RequestStatus.REJECTED, RequestStatus.CANCELLED));
        ALLOWED.put(RequestStatus.ASSIGNED, EnumSet.of(RequestStatus.IN_PROGRESS, RequestStatus.REJECTED, RequestStatus.ASSIGNED));
        ALLOWED.put(RequestStatus.IN_PROGRESS, EnumSet.of(RequestStatus.DONE));
        ALLOWED.put(RequestStatus.DONE, EnumSet.of(RequestStatus.CLOSED));
        ALLOWED.put(RequestStatus.CLOSED, EnumSet.of(RequestStatus.ASSIGNED));
        ALLOWED.put(RequestStatus.REJECTED, EnumSet.noneOf(RequestStatus.class));
        ALLOWED.put(RequestStatus.CANCELLED, EnumSet.noneOf(RequestStatus.class));
    }

    public void validateTransition(RequestStatus from, RequestStatus to, RequestType type) {
        if (from == null || to == null) {
            throw new IllegalStatusTransitionException("Статус не может быть null");
        }
        Set<RequestStatus> targets = ALLOWED.getOrDefault(from, EnumSet.noneOf(RequestStatus.class));
        if (!targets.contains(to)) {
            throw new IllegalStatusTransitionException(from, to);
        }
    }

    public boolean isTerminal(RequestStatus status) {
        return status == RequestStatus.CLOSED
                || status == RequestStatus.REJECTED
                || status == RequestStatus.CANCELLED;
    }
}
