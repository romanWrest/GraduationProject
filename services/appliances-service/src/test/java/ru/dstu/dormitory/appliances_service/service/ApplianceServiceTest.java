package ru.dstu.dormitory.appliances_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.dstu.dormitory.appliances_service.client.ResidentsFacade;
import ru.dstu.dormitory.appliances_service.client.dto.ResidentDto;
import ru.dstu.dormitory.appliances_service.domain.enums.ApplianceStatus;
import ru.dstu.dormitory.appliances_service.domain.enums.ApplianceType;
import ru.dstu.dormitory.appliances_service.domain.model.Appliance;
import ru.dstu.dormitory.appliances_service.domain.repo.ApplianceHistoryRepository;
import ru.dstu.dormitory.appliances_service.domain.repo.ApplianceRepository;
import ru.dstu.dormitory.appliances_service.domain.statemachine.ApplianceStateMachine;
import ru.dstu.dormitory.appliances_service.exception.ApplianceNotFoundException;
import ru.dstu.dormitory.appliances_service.exception.IllegalApplianceTransitionException;
import ru.dstu.dormitory.appliances_service.exception.RoomPowerLimitExceededException;
import ru.dstu.dormitory.appliances_service.service.Impl.ApplianceServiceImpl;
import ru.dstu.dormitory.appliances_service.service.event.ApplianceApprovedEvent;
import ru.dstu.dormitory.appliances_service.service.event.ApplianceRegisteredEvent;
import ru.dstu.dormitory.appliances_service.service.event.ApplianceRejectedEvent;
import ru.dstu.dormitory.appliances_service.service.event.ApplianceRevokedEvent;
import ru.dstu.dormitory.appliances_service.service.event.EventPublisher;
import ru.dstu.dormitory.appliances_service.service.event.EventTypes;
import ru.dstu.dormitory.appliances_service.web.dto.request.RegisterApplianceRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ApplianceServiceTest {

    @Mock
    private ApplianceRepository applianceRepository;
    @Mock
    private ApplianceHistoryRepository historyRepository;
    @Mock
    private ResidentsFacade residentsFacade;
    @Mock
    private RoomPowerService roomPowerService;
    @Mock
    private EventPublisher eventPublisher;

    private ApplianceStateMachine stateMachine;
    private ApplianceServiceImpl service;

    private UUID userId;
    private UUID residentId;
    private UUID roomId;
    private UUID actorId;
    private ResidentDto resident;

    @BeforeEach
    void setUp() {
        stateMachine = new ApplianceStateMachine();
        service = new ApplianceServiceImpl(applianceRepository, historyRepository,
                stateMachine, residentsFacade, roomPowerService, eventPublisher);

        userId = UUID.randomUUID();
        residentId = UUID.randomUUID();
        roomId = UUID.randomUUID();
        actorId = UUID.randomUUID();
        resident = new ResidentDto(residentId, userId, "STUDENT", null, null, null,
                null, null, roomId, "101", null, null, null, null);

        when(applianceRepository.save(any(Appliance.class))).thenAnswer(inv -> {
            Appliance arg = inv.getArgument(0);
            if (arg.getId() == null) {
                arg.setId(UUID.randomUUID());
            }
            return arg;
        });
    }

    @Test
    @DisplayName("Регистрация: создаёт прибор PENDING + событие ApplianceRegistered")
    void register_createsPending() {
        when(residentsFacade.requireResidentByUser(userId)).thenReturn(resident);

        Appliance appliance = service.register(userId,
                new RegisterApplianceRequest(ApplianceType.KETTLE, "Bosch", "TWK", 1500, null, null));

        assertThat(appliance.getStatus()).isEqualTo(ApplianceStatus.PENDING);
        assertThat(appliance.getResidentId()).isEqualTo(residentId);
        assertThat(appliance.getUserId()).isEqualTo(userId);
        assertThat(appliance.getRoomId()).isEqualTo(roomId);
        assertThat(appliance.getPowerWatts()).isEqualTo(1500);

        verify(eventPublisher).publish(eq(EventTypes.AGGREGATE_APPLIANCE), any(),
                eq(EventTypes.APPLIANCE_REGISTERED), any(ApplianceRegisteredEvent.class));
    }

    @Test
    @DisplayName("approve: PENDING → APPROVED + проверяет лимит мощности")
    void approve_pendingToApproved() {
        Appliance pending = pendingAppliance(2000);
        when(applianceRepository.findById(pending.getId())).thenReturn(Optional.of(pending));

        Appliance result = service.approve(pending.getId(), actorId, "ok");

        assertThat(result.getStatus()).isEqualTo(ApplianceStatus.APPROVED);
        assertThat(result.getDecisionBy()).isEqualTo(actorId);
        assertThat(result.getDecisionAt()).isNotNull();
        verify(roomPowerService).requireWithinLimit(roomId, 2000);
        verify(eventPublisher).publish(any(), any(), eq(EventTypes.APPLIANCE_APPROVED),
                any(ApplianceApprovedEvent.class));
    }

    @Test
    @DisplayName("approve: лимит превышен → 409, запись остаётся PENDING")
    void approve_exceedsLimit() {
        Appliance pending = pendingAppliance(3000);
        when(applianceRepository.findById(pending.getId())).thenReturn(Optional.of(pending));
        doThrow(new RoomPowerLimitExceededException("over"))
                .when(roomPowerService).requireWithinLimit(roomId, 3000);

        assertThatThrownBy(() -> service.approve(pending.getId(), actorId, null))
                .isInstanceOf(RoomPowerLimitExceededException.class);
        verify(applianceRepository, never()).save(any());
    }

    @Test
    @DisplayName("approve: уже APPROVED → IllegalApplianceTransitionException")
    void approve_alreadyApproved() {
        Appliance approved = pendingAppliance(1500);
        approved.setStatus(ApplianceStatus.APPROVED);
        when(applianceRepository.findById(approved.getId())).thenReturn(Optional.of(approved));

        assertThatThrownBy(() -> service.approve(approved.getId(), actorId, null))
                .isInstanceOf(IllegalApplianceTransitionException.class);
    }

    @Test
    @DisplayName("approve: roomId=null → лимит не проверяется")
    void approve_nullRoomSkipsLimit() {
        Appliance pending = pendingAppliance(1500);
        pending.setRoomId(null);
        when(applianceRepository.findById(pending.getId())).thenReturn(Optional.of(pending));

        service.approve(pending.getId(), actorId, null);

        verify(roomPowerService, never()).requireWithinLimit(any(), anyInt());
    }

    @Test
    @DisplayName("reject: PENDING → REJECTED + событие ApplianceRejected")
    void reject_pendingToRejected() {
        Appliance pending = pendingAppliance(1500);
        when(applianceRepository.findById(pending.getId())).thenReturn(Optional.of(pending));

        Appliance result = service.reject(pending.getId(), actorId, "не безопасно");

        assertThat(result.getStatus()).isEqualTo(ApplianceStatus.REJECTED);
        assertThat(result.getDecisionReason()).isEqualTo("не безопасно");
        verify(eventPublisher).publish(any(), any(), eq(EventTypes.APPLIANCE_REJECTED),
                any(ApplianceRejectedEvent.class));
    }

    @Test
    @DisplayName("revoke: APPROVED → REVOKED + событие ApplianceRevoked autoRevoked=false")
    void revoke_approvedToRevoked() {
        Appliance approved = pendingAppliance(1500);
        approved.setStatus(ApplianceStatus.APPROVED);
        when(applianceRepository.findById(approved.getId())).thenReturn(Optional.of(approved));

        Appliance result = service.revoke(approved.getId(), actorId, "нарушение");

        assertThat(result.getStatus()).isEqualTo(ApplianceStatus.REVOKED);
        ArgumentCaptor<Object> payload = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publish(any(), any(), eq(EventTypes.APPLIANCE_REVOKED), payload.capture());
        ApplianceRevokedEvent ev = (ApplianceRevokedEvent) payload.getValue();
        assertThat(ev.autoRevoked()).isFalse();
        assertThat(ev.decisionBy()).isEqualTo(actorId);
    }

    @Test
    @DisplayName("revoke: PENDING → REVOKED запрещён")
    void revoke_fromPendingForbidden() {
        Appliance pending = pendingAppliance(1500);
        when(applianceRepository.findById(pending.getId())).thenReturn(Optional.of(pending));

        assertThatThrownBy(() -> service.revoke(pending.getId(), actorId, "x"))
                .isInstanceOf(IllegalApplianceTransitionException.class);
    }

    @Test
    @DisplayName("autoRevokeForResident: все APPROVED-приборы → REVOKED autoRevoked=true")
    void autoRevoke() {
        Appliance a1 = approvedAppliance(1000);
        Appliance a2 = approvedAppliance(800);
        when(applianceRepository.findByResidentIdAndStatus(residentId, ApplianceStatus.APPROVED))
                .thenReturn(List.of(a1, a2));

        int processed = service.autoRevokeForResident(residentId, userId, "Жилец выселен");

        assertThat(processed).isEqualTo(2);
        assertThat(a1.getStatus()).isEqualTo(ApplianceStatus.REVOKED);
        assertThat(a2.getStatus()).isEqualTo(ApplianceStatus.REVOKED);

        ArgumentCaptor<Object> payload = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, times(2))
                .publish(any(), any(), eq(EventTypes.APPLIANCE_REVOKED), payload.capture());
        for (Object p : payload.getAllValues()) {
            assertThat(((ApplianceRevokedEvent) p).autoRevoked()).isTrue();
            assertThat(((ApplianceRevokedEvent) p).decisionBy()).isNull();
        }
    }

    @Test
    @DisplayName("autoRevokeForResident: нет APPROVED → 0 без публикаций")
    void autoRevokeNothing() {
        when(applianceRepository.findByResidentIdAndStatus(residentId, ApplianceStatus.APPROVED))
                .thenReturn(List.of());

        int processed = service.autoRevokeForResident(residentId, userId, "x");

        assertThat(processed).isZero();
        verify(eventPublisher, never()).publish(any(), any(), any(), any());
    }

    @Test
    @DisplayName("getById: не найден → ApplianceNotFoundException")
    void getByIdNotFound() {
        UUID id = UUID.randomUUID();
        when(applianceRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(ApplianceNotFoundException.class);
    }

    private Appliance pendingAppliance(int powerWatts) {
        return Appliance.builder()
                .id(UUID.randomUUID())
                .residentId(residentId)
                .userId(userId)
                .roomId(roomId)
                .type(ApplianceType.KETTLE)
                .powerWatts(powerWatts)
                .status(ApplianceStatus.PENDING)
                .build();
    }

    private Appliance approvedAppliance(int powerWatts) {
        Appliance a = pendingAppliance(powerWatts);
        a.setStatus(ApplianceStatus.APPROVED);
        return a;
    }
}
