package ru.dstu.dormitory.appliances_service.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.dstu.dormitory.appliances_service.domain.enums.ApplianceStatus;
import ru.dstu.dormitory.appliances_service.domain.statemachine.ApplianceStateMachine;
import ru.dstu.dormitory.appliances_service.exception.IllegalApplianceTransitionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApplianceStateMachineTest {

    private final ApplianceStateMachine sm = new ApplianceStateMachine();

    @Test
    @DisplayName("PENDING → APPROVED разрешено")
    void pendingToApproved() {
        assertThat(sm.canTransition(ApplianceStatus.PENDING, ApplianceStatus.APPROVED)).isTrue();
    }

    @Test
    @DisplayName("PENDING → REJECTED разрешено")
    void pendingToRejected() {
        assertThat(sm.canTransition(ApplianceStatus.PENDING, ApplianceStatus.REJECTED)).isTrue();
    }

    @Test
    @DisplayName("APPROVED → REVOKED разрешено")
    void approvedToRevoked() {
        assertThat(sm.canTransition(ApplianceStatus.APPROVED, ApplianceStatus.REVOKED)).isTrue();
    }

    @Test
    @DisplayName("PENDING → REVOKED запрещено")
    void pendingToRevokedForbidden() {
        assertThat(sm.canTransition(ApplianceStatus.PENDING, ApplianceStatus.REVOKED)).isFalse();
    }

    @Test
    @DisplayName("REJECTED → APPROVED запрещено")
    void rejectedToApprovedForbidden() {
        assertThat(sm.canTransition(ApplianceStatus.REJECTED, ApplianceStatus.APPROVED)).isFalse();
    }

    @Test
    @DisplayName("REVOKED → APPROVED запрещено")
    void revokedToApprovedForbidden() {
        assertThat(sm.canTransition(ApplianceStatus.REVOKED, ApplianceStatus.APPROVED)).isFalse();
    }

    @Test
    @DisplayName("APPROVED → APPROVED запрещено")
    void approvedToApprovedForbidden() {
        assertThat(sm.canTransition(ApplianceStatus.APPROVED, ApplianceStatus.APPROVED)).isFalse();
    }

    @Test
    @DisplayName("Терминальные REJECTED и REVOKED не выходят дальше")
    void terminalStatesAreFinal() {
        for (ApplianceStatus to : ApplianceStatus.values()) {
            assertThat(sm.canTransition(ApplianceStatus.REJECTED, to)).isFalse();
            assertThat(sm.canTransition(ApplianceStatus.REVOKED, to)).isFalse();
        }
    }

    @Test
    @DisplayName("requireTransition бросает исключение при недопустимом переходе")
    void requireTransitionThrows() {
        assertThatThrownBy(() ->
                sm.requireTransition(ApplianceStatus.PENDING, ApplianceStatus.REVOKED))
                .isInstanceOf(IllegalApplianceTransitionException.class);
    }

    @Test
    @DisplayName("null → любой запрещён")
    void nullFromForbidden() {
        assertThat(sm.canTransition(null, ApplianceStatus.APPROVED)).isFalse();
        assertThat(sm.canTransition(ApplianceStatus.PENDING, null)).isFalse();
    }
}
