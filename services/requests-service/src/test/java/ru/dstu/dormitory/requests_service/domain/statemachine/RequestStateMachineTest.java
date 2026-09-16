package ru.dstu.dormitory.requests_service.domain.statemachine;

import org.junit.jupiter.api.Test;
import ru.dstu.dormitory.requests_service.domain.enums.RequestStatus;
import ru.dstu.dormitory.requests_service.domain.enums.RequestType;
import ru.dstu.dormitory.requests_service.exception.IllegalStatusTransitionException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class RequestStateMachineTest {

    private final RequestStateMachine sm = new RequestStateMachine();

    @Test
    void allowsAllDocumentedTransitions() {
        RequestType t = RequestType.REPAIR_ELECTRIC;

        assertDoesNotThrow(() -> sm.validateTransition(RequestStatus.NEW, RequestStatus.IN_REVIEW, t));
        assertDoesNotThrow(() -> sm.validateTransition(RequestStatus.NEW, RequestStatus.CANCELLED, t));

        assertDoesNotThrow(() -> sm.validateTransition(RequestStatus.IN_REVIEW, RequestStatus.ASSIGNED, t));
        assertDoesNotThrow(() -> sm.validateTransition(RequestStatus.IN_REVIEW, RequestStatus.REJECTED, t));
        assertDoesNotThrow(() -> sm.validateTransition(RequestStatus.IN_REVIEW, RequestStatus.CANCELLED, t));

        assertDoesNotThrow(() -> sm.validateTransition(RequestStatus.ASSIGNED, RequestStatus.IN_PROGRESS, t));
        assertDoesNotThrow(() -> sm.validateTransition(RequestStatus.ASSIGNED, RequestStatus.REJECTED, t));
        assertDoesNotThrow(() -> sm.validateTransition(RequestStatus.ASSIGNED, RequestStatus.ASSIGNED, t));

        assertDoesNotThrow(() -> sm.validateTransition(RequestStatus.IN_PROGRESS, RequestStatus.DONE, t));
        assertDoesNotThrow(() -> sm.validateTransition(RequestStatus.DONE, RequestStatus.CLOSED, t));
        assertDoesNotThrow(() -> sm.validateTransition(RequestStatus.CLOSED, RequestStatus.ASSIGNED, t));
    }

    @Test
    void rejectsForbiddenTransitions() {
        RequestType t = RequestType.OTHER;

        assertThrows(IllegalStatusTransitionException.class,
                () -> sm.validateTransition(RequestStatus.NEW, RequestStatus.ASSIGNED, t));
        assertThrows(IllegalStatusTransitionException.class,
                () -> sm.validateTransition(RequestStatus.NEW, RequestStatus.DONE, t));
        assertThrows(IllegalStatusTransitionException.class,
                () -> sm.validateTransition(RequestStatus.NEW, RequestStatus.CLOSED, t));

        assertThrows(IllegalStatusTransitionException.class,
                () -> sm.validateTransition(RequestStatus.IN_REVIEW, RequestStatus.IN_PROGRESS, t));
        assertThrows(IllegalStatusTransitionException.class,
                () -> sm.validateTransition(RequestStatus.IN_REVIEW, RequestStatus.DONE, t));

        assertThrows(IllegalStatusTransitionException.class,
                () -> sm.validateTransition(RequestStatus.ASSIGNED, RequestStatus.DONE, t));
        assertThrows(IllegalStatusTransitionException.class,
                () -> sm.validateTransition(RequestStatus.ASSIGNED, RequestStatus.CLOSED, t));

        assertThrows(IllegalStatusTransitionException.class,
                () -> sm.validateTransition(RequestStatus.IN_PROGRESS, RequestStatus.ASSIGNED, t));

        assertThrows(IllegalStatusTransitionException.class,
                () -> sm.validateTransition(RequestStatus.CLOSED, RequestStatus.IN_PROGRESS, t));
        assertThrows(IllegalStatusTransitionException.class,
                () -> sm.validateTransition(RequestStatus.CLOSED, RequestStatus.DONE, t));

        assertThrows(IllegalStatusTransitionException.class,
                () -> sm.validateTransition(RequestStatus.CANCELLED, RequestStatus.IN_REVIEW, t));
        assertThrows(IllegalStatusTransitionException.class,
                () -> sm.validateTransition(RequestStatus.REJECTED, RequestStatus.IN_REVIEW, t));
    }

    @Test
    void rejectsNullStatuses() {
        assertThrows(IllegalStatusTransitionException.class,
                () -> sm.validateTransition(null, RequestStatus.IN_REVIEW, RequestType.OTHER));
        assertThrows(IllegalStatusTransitionException.class,
                () -> sm.validateTransition(RequestStatus.NEW, null, RequestType.OTHER));
    }

    @Test
    void recognisesTerminalStatuses() {
        assertTrue(sm.isTerminal(RequestStatus.CLOSED));
        assertTrue(sm.isTerminal(RequestStatus.CANCELLED));
        assertTrue(sm.isTerminal(RequestStatus.REJECTED));
        assertFalse(sm.isTerminal(RequestStatus.ASSIGNED));
        assertFalse(sm.isTerminal(RequestStatus.IN_PROGRESS));
    }
}
