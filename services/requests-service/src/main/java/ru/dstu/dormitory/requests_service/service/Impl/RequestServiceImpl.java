package ru.dstu.dormitory.requests_service.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.dstu.dormitory.requests_service.aspect.annotation.LogMethod;
import ru.dstu.dormitory.requests_service.client.ResidentsFacade;
import ru.dstu.dormitory.requests_service.client.dto.ResidentDto;
import ru.dstu.dormitory.requests_service.config.RequestsProperties;
import ru.dstu.dormitory.requests_service.domain.enums.RequestStatus;
import ru.dstu.dormitory.requests_service.domain.enums.RequestType;
import ru.dstu.dormitory.requests_service.domain.enums.RoleCode;
import ru.dstu.dormitory.requests_service.domain.enums.TargetPool;
import ru.dstu.dormitory.requests_service.domain.model.Request;
import ru.dstu.dormitory.requests_service.domain.model.RequestStatusHistory;
import ru.dstu.dormitory.requests_service.domain.repo.RequestRepository;
import ru.dstu.dormitory.requests_service.domain.repo.RequestStatusHistoryRepository;
import ru.dstu.dormitory.requests_service.domain.statemachine.RequestStateMachine;
import ru.dstu.dormitory.requests_service.exception.ForbiddenActionException;
import ru.dstu.dormitory.requests_service.exception.RequestCannotBeCancelledException;
import ru.dstu.dormitory.requests_service.exception.RequestCannotBeReopenedException;
import ru.dstu.dormitory.requests_service.exception.RequestNotFoundException;
import ru.dstu.dormitory.requests_service.security.AccessControl;
import ru.dstu.dormitory.requests_service.security.UserPrincipal;
import ru.dstu.dormitory.requests_service.service.AttachmentService;
import ru.dstu.dormitory.requests_service.service.RequestService;
import ru.dstu.dormitory.requests_service.service.RoutingService;
import ru.dstu.dormitory.requests_service.service.event.EventPublisher;
import ru.dstu.dormitory.requests_service.service.event.EventTypes;
import ru.dstu.dormitory.requests_service.service.event.RequestAssignedEvent;
import ru.dstu.dormitory.requests_service.service.event.RequestClosedEvent;
import ru.dstu.dormitory.requests_service.service.event.RequestCreatedEvent;
import ru.dstu.dormitory.requests_service.service.event.RequestStatusChangedEvent;
import ru.dstu.dormitory.requests_service.util.LogPatterns;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final RequestStatusHistoryRepository historyRepository;
    private final RequestStateMachine stateMachine;
    private final RoutingService routingService;
    private final ResidentsFacade residentsFacade;
    private final EventPublisher eventPublisher;
    private final AttachmentService attachmentService;
    private final AccessControl accessControl;
    private final RequestsProperties requestsProperties;

    @Override
    @LogMethod(value = "Создание заявки", logArgs = {"dto"})
    @Transactional
    public Request create(ru.dstu.dormitory.requests_service.web.dto.CreateRequestDto dto,
                          UserPrincipal actor,
                          List<MultipartFile> files) {
        UUID userId = accessControl.requireUserId(actor);

        ResidentDto resident = residentsFacade.findResidentByUser(userId);
        UUID residentId = resident == null ? null : resident.id();
        UUID roomId = resident == null ? null : resident.roomId();

        TargetPool pool = routingService.resolvePool(dto.type());

        Request entity = Request.builder()
                .authorId(userId)
                .authorResidentId(residentId)
                .roomId(roomId)
                .type(dto.type())
                .title(dto.title())
                .description(dto.description())
                .reason(dto.reason())
                .status(RequestStatus.IN_REVIEW)
                .targetPool(pool)
                .autoClosed(false)
                .build();

        Request saved = requestRepository.save(entity);

        logHistory(saved, RequestStatus.NEW, RequestStatus.IN_REVIEW, userId, "Автомаршрутизация");

        log.info(LogPatterns.REQUEST_CREATED, saved.getId(), saved.getType(), userId, roomId);
        log.info(LogPatterns.REQUEST_ROUTED, saved.getId(), pool);

        eventPublisher.publish(EventTypes.AGGREGATE_REQUEST, saved.getId().toString(),
                EventTypes.REQUEST_CREATED,
                new RequestCreatedEvent(saved.getId(), saved.getType(), userId, roomId, pool, saved.getCreatedAt()));
        eventPublisher.publish(EventTypes.AGGREGATE_REQUEST, saved.getId().toString(),
                EventTypes.REQUEST_STATUS_CHANGED,
                new RequestStatusChangedEvent(saved.getId(), RequestStatus.NEW, RequestStatus.IN_REVIEW,
                        userId, saved.getUpdatedAt(), saved.getType()));

        if (files != null) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    attachmentService.upload(saved.getId(), file, actor);
                }
            }
        }

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Request getById(UUID id) {
        return requestRepository.findById(id).orElseThrow(() -> new RequestNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Request> search(UserPrincipal actor,
                                RequestType type,
                                RequestStatus status,
                                UUID assigneeId,
                                UUID authorId,
                                UUID roomId,
                                Instant from,
                                Instant to,
                                String search,
                                Pageable pageable) {
        Specification<Request> spec = buildBaseSpec(actor);
        spec = appendFilter(spec, type, status, assigneeId, authorId, roomId, from, to, search);
        return requestRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Request> pool(UserPrincipal actor, Pageable pageable) {
        if (actor == null) {
            throw new ForbiddenActionException("Требуется аутентификация");
        }
        if (accessControl.isAdmin(actor)) {
            return requestRepository.findAll(
                    (root, cq, cb) -> cb.equal(root.get("status"), RequestStatus.IN_REVIEW), pageable);
        }
        TargetPool pool = detectPool(actor);
        if (pool == null) {
            throw new ForbiddenActionException("Роль пользователя не соответствует ни одному пулу");
        }
        return requestRepository.findByTargetPoolAndStatus(pool, RequestStatus.IN_REVIEW, pageable);
    }

    @Override
    @LogMethod("Админ: перевод заявки в рассмотрение")
    @Transactional
    public Request review(UUID id, UserPrincipal actor) {
        if (!accessControl.isAdmin(actor)) {
            throw new ForbiddenActionException("Только ADMIN может перевести заявку в IN_REVIEW");
        }
        Request request = getById(id);
        changeStatus(request, RequestStatus.IN_REVIEW, actor.userId(), "Ручной перевод в рассмотрение");
        return requestRepository.save(request);
    }

    @Override
    @LogMethod("Назначение исполнителя")
    @Transactional
    public Request assign(UUID id, UUID assigneeId, Instant scheduledAt, UserPrincipal actor) {
        Request request = getById(id);
        boolean admin = accessControl.isAdmin(actor);
        boolean selfAssignFromPool = !admin
                && request.getStatus() == RequestStatus.IN_REVIEW
                && accessControl.isInTargetPool(actor, request)
                && (assigneeId == null || assigneeId.equals(actor.userId()));

        if (!admin && !selfAssignFromPool) {
            throw new ForbiddenActionException("Назначать заявку может ADMIN или исполнитель из пула (себе)");
        }

        UUID resolvedAssignee = assigneeId != null ? assigneeId
                : (selfAssignFromPool ? actor.userId() : null);
        if (resolvedAssignee == null) {
            throw new ForbiddenActionException("Не указан исполнитель");
        }

        RequestStatus from = request.getStatus();
        stateMachine.validateTransition(from, RequestStatus.ASSIGNED, request.getType());

        request.setAssigneeId(resolvedAssignee);
        request.setScheduledAt(scheduledAt);
        request.setStatus(RequestStatus.ASSIGNED);

        Request saved = requestRepository.save(request);
        logHistory(saved, from, RequestStatus.ASSIGNED, actor.userId(),
                "Назначен исполнитель %s".formatted(resolvedAssignee));

        log.info(LogPatterns.REQUEST_ASSIGNED, saved.getId(), resolvedAssignee, scheduledAt);

        eventPublisher.publish(EventTypes.AGGREGATE_REQUEST, saved.getId().toString(),
                EventTypes.REQUEST_STATUS_CHANGED,
                new RequestStatusChangedEvent(saved.getId(), from, RequestStatus.ASSIGNED,
                        actor.userId(), saved.getUpdatedAt(), saved.getType()));
        eventPublisher.publish(EventTypes.AGGREGATE_REQUEST, saved.getId().toString(),
                EventTypes.REQUEST_ASSIGNED,
                new RequestAssignedEvent(saved.getId(), resolvedAssignee, scheduledAt, saved.getType()));
        return saved;
    }

    @Override
    @LogMethod("Исполнитель начал работу")
    @Transactional
    public Request start(UUID id, UserPrincipal actor) {
        Request request = getById(id);
        if (!accessControl.isAssignee(actor, request)) {
            throw new ForbiddenActionException("Только назначенный исполнитель может начать работу");
        }
        changeStatus(request, RequestStatus.IN_PROGRESS, actor.userId(), "Исполнитель взял в работу");
        return requestRepository.save(request);
    }

    @Override
    @LogMethod("Исполнитель завершил работу")
    @Transactional
    public Request done(UUID id, String resolutionComment, UserPrincipal actor) {
        Request request = getById(id);
        if (!accessControl.isAssignee(actor, request)) {
            throw new ForbiddenActionException("Только назначенный исполнитель может завершить работу");
        }
        request.setResolutionComment(resolutionComment);
        changeStatus(request, RequestStatus.DONE, actor.userId(), "Работы завершены");
        return requestRepository.save(request);
    }

    @Override
    @LogMethod("Автор подтвердил закрытие")
    @Transactional
    public Request confirm(UUID id, UserPrincipal actor) {
        Request request = getById(id);
        if (!accessControl.isAuthor(actor, request) && !accessControl.isAdmin(actor)) {
            throw new ForbiddenActionException("Подтвердить закрытие может только автор или ADMIN");
        }
        return closeRequest(request, actor.userId(), false);
    }

    @Override
    @LogMethod("Отклонение заявки")
    @Transactional
    public Request reject(UUID id, String reason, UserPrincipal actor) {
        Request request = getById(id);
        boolean admin = accessControl.isAdmin(actor);
        boolean executorInPool = request.getStatus() == RequestStatus.IN_REVIEW
                && accessControl.isInTargetPool(actor, request);
        boolean assignedExecutor = request.getStatus() == RequestStatus.ASSIGNED
                && accessControl.isAssignee(actor, request);

        if (!admin && !executorInPool && !assignedExecutor) {
            throw new ForbiddenActionException("Отклонять заявку может ADMIN или исполнитель пула/назначенный");
        }
        request.setRejectionReason(reason);
        changeStatus(request, RequestStatus.REJECTED, actor.userId(), reason);
        Request saved = requestRepository.save(request);
        log.info(LogPatterns.REQUEST_REJECTED, saved.getId(), reason);
        return saved;
    }

    @Override
    @LogMethod("Отзыв заявки автором")
    @Transactional
    public Request cancel(UUID id, String reason, UserPrincipal actor) {
        Request request = getById(id);
        if (!accessControl.isAuthor(actor, request) && !accessControl.isAdmin(actor)) {
            throw new ForbiddenActionException("Отозвать заявку может только автор или ADMIN");
        }
        if (request.getStatus() != RequestStatus.NEW && request.getStatus() != RequestStatus.IN_REVIEW) {
            throw new RequestCannotBeCancelledException(
                    "Нельзя отозвать заявку в статусе %s".formatted(request.getStatus()));
        }
        request.setCancellationReason(reason);
        changeStatus(request, RequestStatus.CANCELLED, actor.userId(), reason);
        Request saved = requestRepository.save(request);
        log.info(LogPatterns.REQUEST_CANCELLED, saved.getId());
        return saved;
    }

    @Override
    @LogMethod("Переоткрытие заявки автором")
    @Transactional
    public Request reopen(UUID id, String reason, UserPrincipal actor) {
        Request request = getById(id);
        if (!accessControl.isAuthor(actor, request) && !accessControl.isAdmin(actor)) {
            throw new ForbiddenActionException("Переоткрыть заявку может только автор или ADMIN");
        }
        if (request.getStatus() != RequestStatus.CLOSED) {
            throw new RequestCannotBeReopenedException(
                    "Переоткрыть можно только заявку в статусе CLOSED");
        }
        Instant closedAt = request.getClosedAt();
        int window = requestsProperties.getReopenWindowDays();
        if (closedAt == null
                || Duration.between(closedAt, Instant.now()).toDays() > window) {
            throw new RequestCannotBeReopenedException(
                    "Окно переоткрытия (%d дней) истекло".formatted(window));
        }
        if (request.getAssigneeId() == null) {
            throw new RequestCannotBeReopenedException(
                    "У заявки нет исполнителя для переназначения");
        }

        RequestStatus from = request.getStatus();
        request.setReopenReason(reason);
        request.setStatus(RequestStatus.ASSIGNED);
        request.setClosedAt(null);
        request.setAutoClosed(false);

        Request saved = requestRepository.save(request);
        logHistory(saved, from, RequestStatus.ASSIGNED, actor.userId(), "Переоткрытие: %s".formatted(reason));

        log.info(LogPatterns.REQUEST_REOPENED, saved.getId());

        eventPublisher.publish(EventTypes.AGGREGATE_REQUEST, saved.getId().toString(),
                EventTypes.REQUEST_STATUS_CHANGED,
                new RequestStatusChangedEvent(saved.getId(), from, RequestStatus.ASSIGNED,
                        actor.userId(), saved.getUpdatedAt(), saved.getType()));
        eventPublisher.publish(EventTypes.AGGREGATE_REQUEST, saved.getId().toString(),
                EventTypes.REQUEST_ASSIGNED,
                new RequestAssignedEvent(saved.getId(), saved.getAssigneeId(), saved.getScheduledAt(), saved.getType()));
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestStatusHistory> history(UUID id, UserPrincipal actor) {
        Request request = getById(id);
        accessControl.requireCanView(actor, request);
        return historyRepository.findByRequestIdOrderByChangedAtAsc(id);
    }

    /**
     * Меняет статус, пишет историю, публикует событие.
     */
    private void changeStatus(Request request, RequestStatus to, UUID actorId, String comment) {
        RequestStatus from = request.getStatus();
        stateMachine.validateTransition(from, to, request.getType());
        request.setStatus(to);
        if (to == RequestStatus.CLOSED && request.getClosedAt() == null) {
            request.setClosedAt(Instant.now());
        }
        logHistory(request, from, to, actorId, comment);
        log.info(LogPatterns.REQUEST_STATUS_CHANGED, request.getId(), from, to);
        eventPublisher.publish(EventTypes.AGGREGATE_REQUEST, request.getId().toString(),
                EventTypes.REQUEST_STATUS_CHANGED,
                new RequestStatusChangedEvent(request.getId(), from, to, actorId, Instant.now(), request.getType()));
    }

    private Request closeRequest(Request request, UUID actorId, boolean autoClosed) {
        RequestStatus from = request.getStatus();
        stateMachine.validateTransition(from, RequestStatus.CLOSED, request.getType());
        request.setStatus(RequestStatus.CLOSED);
        request.setClosedAt(Instant.now());
        request.setAutoClosed(autoClosed);

        Request saved = requestRepository.save(request);
        logHistory(saved, from, RequestStatus.CLOSED, actorId,
                autoClosed ? "Автозакрытие" : "Закрытие по подтверждению автора");

        log.info(LogPatterns.REQUEST_STATUS_CHANGED, saved.getId(), from, RequestStatus.CLOSED);
        int resolutionLen = saved.getResolutionComment() == null ? 0 : saved.getResolutionComment().length();
        log.info(LogPatterns.REQUEST_CLOSED, saved.getId(), resolutionLen);

        eventPublisher.publish(EventTypes.AGGREGATE_REQUEST, saved.getId().toString(),
                EventTypes.REQUEST_STATUS_CHANGED,
                new RequestStatusChangedEvent(saved.getId(), from, RequestStatus.CLOSED,
                        actorId, saved.getUpdatedAt(), saved.getType()));
        eventPublisher.publish(EventTypes.AGGREGATE_REQUEST, saved.getId().toString(),
                EventTypes.REQUEST_CLOSED,
                new RequestClosedEvent(saved.getId(), autoClosed, saved.getClosedAt(), saved.getType()));
        return saved;
    }

    public Request autoClose(Request request) {
        log.info(LogPatterns.REQUEST_AUTOCLOSED, request.getId());
        return closeRequest(request, null, true);
    }

    /**
     * Записать в историю смены статусов.
     */
    private void logHistory(Request request, RequestStatus from, RequestStatus to, UUID actorId, String comment) {
        RequestStatusHistory row = RequestStatusHistory.builder()
                .request(request)
                .fromStatus(from)
                .toStatus(to)
                .actorId(actorId)
                .comment(comment)
                .build();
        historyRepository.save(row);
    }

    /**
     * Базовый spec — ограничение видимости по ролям.
     */
    private Specification<Request> buildBaseSpec(UserPrincipal actor) {
        if (actor == null) {
            throw new ForbiddenActionException("Требуется аутентификация");
        }
        if (accessControl.isAdmin(actor)) {
            return (root, cq, cb) -> cb.conjunction();
        }
        TargetPool pool = detectPool(actor);
        UUID userId = actor.userId();
        return (root, cq, cb) -> {
            var authorEq = cb.equal(root.get("authorId"), userId);
            var assigneeEq = cb.equal(root.get("assigneeId"), userId);
            if (pool != null) {
                var poolAndReview = cb.and(
                        cb.equal(root.get("targetPool"), pool),
                        cb.equal(root.get("status"), RequestStatus.IN_REVIEW)
                );
                return cb.or(authorEq, assigneeEq, poolAndReview);
            }
            return cb.or(authorEq, assigneeEq);
        };
    }

    private Specification<Request> appendFilter(Specification<Request> spec,
                                                RequestType type,
                                                RequestStatus status,
                                                UUID assigneeId,
                                                UUID authorId,
                                                UUID roomId,
                                                Instant from,
                                                Instant to,
                                                String search) {
        Specification<Request> result = spec;
        if (type != null) {
            result = result.and((root, cq, cb) -> cb.equal(root.get("type"), type));
        }
        if (status != null) {
            result = result.and((root, cq, cb) -> cb.equal(root.get("status"), status));
        }
        if (assigneeId != null) {
            result = result.and((root, cq, cb) -> cb.equal(root.get("assigneeId"), assigneeId));
        }
        if (authorId != null) {
            result = result.and((root, cq, cb) -> cb.equal(root.get("authorId"), authorId));
        }
        if (roomId != null) {
            result = result.and((root, cq, cb) -> cb.equal(root.get("roomId"), roomId));
        }
        if (from != null) {
            result = result.and((root, cq, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from));
        }
        if (to != null) {
            result = result.and((root, cq, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to));
        }
        if (search != null && !search.isBlank()) {
            String like = "%" + search.toLowerCase() + "%";
            result = result.and((root, cq, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), like),
                    cb.like(cb.lower(root.get("description")), like)
            ));
        }
        return result;
    }

    private TargetPool detectPool(UserPrincipal actor) {
        List<TargetPool> pools = new ArrayList<>();
        if (actor.roles().contains(RoleCode.EXECUTOR_ELECTRIC)) pools.add(TargetPool.EXECUTOR_ELECTRIC);
        if (actor.roles().contains(RoleCode.EXECUTOR_PLUMBING)) pools.add(TargetPool.EXECUTOR_PLUMBING);
        if (actor.roles().contains(RoleCode.EXECUTOR_CARPENTRY)) pools.add(TargetPool.EXECUTOR_CARPENTRY);
        if (actor.roles().contains(RoleCode.EXECUTOR_GAS)) pools.add(TargetPool.EXECUTOR_GAS);
        if (actor.roles().contains(RoleCode.PROPERTY_MANAGER)) pools.add(TargetPool.PROPERTY_MANAGER);
        return pools.isEmpty() ? null : pools.get(0);
    }
}
