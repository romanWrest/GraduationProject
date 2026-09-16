package ru.dstu.dormitory.requests_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.dstu.dormitory.requests_service.client.ResidentsFacade;
import ru.dstu.dormitory.requests_service.client.dto.ResidentDto;
import ru.dstu.dormitory.requests_service.config.RequestsProperties;
import ru.dstu.dormitory.requests_service.domain.enums.RequestStatus;
import ru.dstu.dormitory.requests_service.domain.enums.RequestType;
import ru.dstu.dormitory.requests_service.domain.enums.RoleCode;
import ru.dstu.dormitory.requests_service.domain.enums.TargetPool;
import ru.dstu.dormitory.requests_service.domain.model.Request;
import ru.dstu.dormitory.requests_service.domain.repo.RequestRepository;
import ru.dstu.dormitory.requests_service.domain.repo.RequestStatusHistoryRepository;
import ru.dstu.dormitory.requests_service.domain.statemachine.RequestStateMachine;
import ru.dstu.dormitory.requests_service.exception.ForbiddenActionException;
import ru.dstu.dormitory.requests_service.exception.RequestCannotBeCancelledException;
import ru.dstu.dormitory.requests_service.exception.RequestCannotBeReopenedException;
import ru.dstu.dormitory.requests_service.security.AccessControl;
import ru.dstu.dormitory.requests_service.security.UserPrincipal;
import ru.dstu.dormitory.requests_service.service.Impl.RequestServiceImpl;
import ru.dstu.dormitory.requests_service.service.event.EventPublisher;
import ru.dstu.dormitory.requests_service.web.dto.CreateRequestDto;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {

    @Mock
    RequestRepository requestRepository;
    @Mock
    RequestStatusHistoryRepository historyRepository;
    @Mock
    RoutingService routingService;
    @Mock
    ResidentsFacade residentsFacade;
    @Mock
    EventPublisher eventPublisher;
    @Mock
    AttachmentService attachmentService;

    RequestStateMachine stateMachine = new RequestStateMachine();
    AccessControl accessControl = new AccessControl();
    RequestsProperties properties = new RequestsProperties();

    RequestServiceImpl service;

    UUID authorId = UUID.randomUUID();
    UUID residentId = UUID.randomUUID();
    UUID roomId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new RequestServiceImpl(
                requestRepository,
                historyRepository,
                stateMachine,
                routingService,
                residentsFacade,
                eventPublisher,
                attachmentService,
                accessControl,
                properties
        );
    }

    @Test
    void createRequestSubstitutesRoomFromResidents() {
        UserPrincipal actor = new UserPrincipal(authorId, "a@b.c", Set.of(RoleCode.RESIDENT));
        CreateRequestDto dto = new CreateRequestDto(RequestType.REPAIR_PLUMBING, "Кран", "Течёт", null);

        when(residentsFacade.findResidentByUser(authorId)).thenReturn(
                new ResidentDto(residentId, authorId, "STUDENT", null, null, null, null, null,
                        roomId, "305А", null, null, null, null));
        when(routingService.resolvePool(RequestType.REPAIR_PLUMBING)).thenReturn(TargetPool.EXECUTOR_PLUMBING);
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> {
            Request r = inv.getArgument(0);
            if (r.getId() == null) {
                r.setId(UUID.randomUUID());
            }
            r.setCreatedAt(Instant.now());
            r.setUpdatedAt(Instant.now());
            return r;
        });

        Request result = service.create(dto, actor, null);

        assertEquals(authorId, result.getAuthorId());
        assertEquals(residentId, result.getAuthorResidentId());
        assertEquals(roomId, result.getRoomId());
        assertEquals(RequestStatus.IN_REVIEW, result.getStatus());
        assertEquals(TargetPool.EXECUTOR_PLUMBING, result.getTargetPool());
    }

    @Test
    void createWithoutPrincipalThrowsForbidden() {
        CreateRequestDto dto = new CreateRequestDto(RequestType.OTHER, "t", "d", null);
        assertThrows(ForbiddenActionException.class, () -> service.create(dto, null, null));
    }

    @Test
    void cancelRejectsWhenStatusIsProgress() {
        UserPrincipal actor = new UserPrincipal(authorId, "a@b.c", Set.of(RoleCode.RESIDENT));
        Request r = Request.builder()
                .id(UUID.randomUUID())
                .authorId(authorId)
                .type(RequestType.OTHER)
                .status(RequestStatus.IN_PROGRESS)
                .build();
        when(requestRepository.findById(r.getId())).thenReturn(java.util.Optional.of(r));
        assertThrows(RequestCannotBeCancelledException.class, () -> service.cancel(r.getId(), "передумал", actor));
    }

    @Test
    void reopenRejectsOutsideWindow() {
        UserPrincipal actor = new UserPrincipal(authorId, "a@b.c", Set.of(RoleCode.RESIDENT));
        Request r = Request.builder()
                .id(UUID.randomUUID())
                .authorId(authorId)
                .type(RequestType.OTHER)
                .status(RequestStatus.CLOSED)
                .assigneeId(UUID.randomUUID())
                .closedAt(Instant.now().minus(30, ChronoUnit.DAYS))
                .build();
        when(requestRepository.findById(r.getId())).thenReturn(java.util.Optional.of(r));
        assertThrows(RequestCannotBeReopenedException.class, () -> service.reopen(r.getId(), "почему", actor));
    }

    @Test
    void confirmByAuthorClosesRequestAndPublishesEvents() {
        UserPrincipal actor = new UserPrincipal(authorId, "a@b.c", Set.of(RoleCode.RESIDENT));
        Request r = Request.builder()
                .id(UUID.randomUUID())
                .authorId(authorId)
                .type(RequestType.OTHER)
                .status(RequestStatus.DONE)
                .build();
        when(requestRepository.findById(r.getId())).thenReturn(java.util.Optional.of(r));
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> {
            Request x = inv.getArgument(0);
            x.setUpdatedAt(Instant.now());
            return x;
        });

        Request result = service.confirm(r.getId(), actor);

        assertEquals(RequestStatus.CLOSED, result.getStatus());
        ArgumentCaptor<String> eventTypes = ArgumentCaptor.forClass(String.class);
        verify(eventPublisher, org.mockito.Mockito.atLeast(2))
                .publish(anyString(), anyString(), eventTypes.capture(), any());
    }

    @Test
    void assignRequiresAdminOrPoolMember() {
        UserPrincipal randomUser = new UserPrincipal(UUID.randomUUID(), "x@y.z", Set.of(RoleCode.RESIDENT));
        Request r = Request.builder()
                .id(UUID.randomUUID())
                .authorId(authorId)
                .type(RequestType.REPAIR_ELECTRIC)
                .status(RequestStatus.IN_REVIEW)
                .targetPool(TargetPool.EXECUTOR_ELECTRIC)
                .build();
        when(requestRepository.findById(r.getId())).thenReturn(java.util.Optional.of(r));
        assertThrows(ForbiddenActionException.class,
                () -> service.assign(r.getId(), UUID.randomUUID(), null, randomUser));
        verify(requestRepository, never()).save(any(Request.class));
    }
}
