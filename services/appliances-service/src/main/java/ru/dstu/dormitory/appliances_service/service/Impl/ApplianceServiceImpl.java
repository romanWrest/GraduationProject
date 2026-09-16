package ru.dstu.dormitory.appliances_service.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.appliances_service.aspect.annotation.LogMethod;
import ru.dstu.dormitory.appliances_service.client.ResidentsFacade;
import ru.dstu.dormitory.appliances_service.client.dto.ResidentDto;
import ru.dstu.dormitory.appliances_service.domain.enums.ApplianceStatus;
import ru.dstu.dormitory.appliances_service.domain.enums.ApplianceType;
import ru.dstu.dormitory.appliances_service.domain.model.Appliance;
import ru.dstu.dormitory.appliances_service.domain.model.ApplianceHistory;
import ru.dstu.dormitory.appliances_service.domain.repo.ApplianceHistoryRepository;
import ru.dstu.dormitory.appliances_service.domain.repo.ApplianceRepository;
import ru.dstu.dormitory.appliances_service.domain.repo.ApplianceSpecifications;
import ru.dstu.dormitory.appliances_service.domain.statemachine.ApplianceStateMachine;
import ru.dstu.dormitory.appliances_service.exception.ApplianceNotFoundException;
import ru.dstu.dormitory.appliances_service.service.ApplianceService;
import ru.dstu.dormitory.appliances_service.service.RoomPowerService;
import ru.dstu.dormitory.appliances_service.service.event.ApplianceApprovedEvent;
import ru.dstu.dormitory.appliances_service.service.event.ApplianceRegisteredEvent;
import ru.dstu.dormitory.appliances_service.service.event.ApplianceRejectedEvent;
import ru.dstu.dormitory.appliances_service.service.event.ApplianceRevokedEvent;
import ru.dstu.dormitory.appliances_service.service.event.EventPublisher;
import ru.dstu.dormitory.appliances_service.service.event.EventTypes;
import ru.dstu.dormitory.appliances_service.util.LogPatterns;
import ru.dstu.dormitory.appliances_service.web.dto.request.RegisterApplianceRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplianceServiceImpl implements ApplianceService {

    private final ApplianceRepository applianceRepository;
    private final ApplianceHistoryRepository historyRepository;
    private final ApplianceStateMachine stateMachine;
    private final ResidentsFacade residentsFacade;
    private final RoomPowerService roomPowerService;
    private final EventPublisher eventPublisher;

    @Override
    @LogMethod
    @Transactional
    public Appliance register(UUID userId, RegisterApplianceRequest request) {
        ResidentDto resident = residentsFacade.requireResidentByUser(userId);

        Appliance appliance = Appliance.builder()
                .residentId(resident.id())
                .userId(resident.userId())
                .roomId(resident.roomId())
                .type(request.type())
                .brand(request.brand())
                .model(request.model())
                .powerWatts(request.powerWatts())
                .photoUrl(request.photoUrl())
                .notes(request.notes())
                .status(ApplianceStatus.PENDING)
                .build();
        Appliance saved = applianceRepository.save(appliance);

        historyRepository.save(ApplianceHistory.builder()
                .appliance(saved)
                .fromStatus(null)
                .toStatus(ApplianceStatus.PENDING)
                .actorId(userId)
                .build());

        log.info(LogPatterns.APPLIANCE_REGISTERED, saved.getId(), saved.getResidentId(),
                saved.getType(), saved.getPowerWatts());

        eventPublisher.publish(EventTypes.AGGREGATE_APPLIANCE, saved.getId().toString(),
                EventTypes.APPLIANCE_REGISTERED,
                new ApplianceRegisteredEvent(saved.getId(), saved.getResidentId(),
                        saved.getUserId(), saved.getRoomId(),
                        saved.getType(), saved.getPowerWatts()));

        return saved;
    }

    @Override
    @LogMethod
    @Transactional
    public Appliance approve(UUID id, UUID actorId, String comment) {
        Appliance appliance = getById(id);
        ApplianceStatus from = appliance.getStatus();
        stateMachine.requireTransition(from, ApplianceStatus.APPROVED);

        if (appliance.getRoomId() != null) {
            roomPowerService.requireWithinLimit(appliance.getRoomId(), appliance.getPowerWatts());
        }

        Instant now = Instant.now();
        appliance.setStatus(ApplianceStatus.APPROVED);
        appliance.setDecisionBy(actorId);
        appliance.setDecisionAt(now);
        appliance.setDecisionReason(comment);
        Appliance saved = applianceRepository.save(appliance);

        historyRepository.save(ApplianceHistory.builder()
                .appliance(saved)
                .fromStatus(from)
                .toStatus(ApplianceStatus.APPROVED)
                .actorId(actorId)
                .reason(comment)
                .build());

        log.info(LogPatterns.APPLIANCE_APPROVED, saved.getId(), actorId);

        eventPublisher.publish(EventTypes.AGGREGATE_APPLIANCE, saved.getId().toString(),
                EventTypes.APPLIANCE_APPROVED,
                new ApplianceApprovedEvent(saved.getId(), saved.getResidentId(), actorId, now));

        return saved;
    }

    @Override
    @LogMethod
    @Transactional
    public Appliance reject(UUID id, UUID actorId, String reason) {
        Appliance appliance = getById(id);
        ApplianceStatus from = appliance.getStatus();
        stateMachine.requireTransition(from, ApplianceStatus.REJECTED);

        appliance.setStatus(ApplianceStatus.REJECTED);
        appliance.setDecisionBy(actorId);
        appliance.setDecisionAt(Instant.now());
        appliance.setDecisionReason(reason);
        Appliance saved = applianceRepository.save(appliance);

        historyRepository.save(ApplianceHistory.builder()
                .appliance(saved)
                .fromStatus(from)
                .toStatus(ApplianceStatus.REJECTED)
                .actorId(actorId)
                .reason(reason)
                .build());

        log.info(LogPatterns.APPLIANCE_REJECTED, saved.getId(), actorId, reason);

        eventPublisher.publish(EventTypes.AGGREGATE_APPLIANCE, saved.getId().toString(),
                EventTypes.APPLIANCE_REJECTED,
                new ApplianceRejectedEvent(saved.getId(), saved.getResidentId(), reason, actorId));

        return saved;
    }

    @Override
    @LogMethod
    @Transactional
    public Appliance revoke(UUID id, UUID actorId, String reason) {
        Appliance appliance = getById(id);
        ApplianceStatus from = appliance.getStatus();
        stateMachine.requireTransition(from, ApplianceStatus.REVOKED);

        Appliance saved = applyRevoke(appliance, from, actorId, reason, false);
        log.info(LogPatterns.APPLIANCE_REVOKED, saved.getId(), actorId, reason);
        return saved;
    }

    @Override
    @LogMethod
    @Transactional
    public int autoRevokeForResident(UUID residentId, UUID userId, String reason) {
        List<Appliance> approved = residentId != null
                ? applianceRepository.findByResidentIdAndStatus(residentId, ApplianceStatus.APPROVED)
                : applianceRepository.findByUserIdAndStatus(userId, ApplianceStatus.APPROVED);

        for (Appliance appliance : approved) {
            ApplianceStatus from = appliance.getStatus();
            applyRevoke(appliance, from, null, reason, true);
            log.info(LogPatterns.APPLIANCE_AUTO_REVOKED, appliance.getId(), appliance.getResidentId());
        }
        return approved.size();
    }

    private Appliance applyRevoke(Appliance appliance, ApplianceStatus from,
                                  UUID actorId, String reason, boolean autoRevoked) {
        appliance.setStatus(ApplianceStatus.REVOKED);
        appliance.setDecisionBy(actorId);
        appliance.setDecisionAt(Instant.now());
        appliance.setDecisionReason(reason);
        Appliance saved = applianceRepository.save(appliance);

        historyRepository.save(ApplianceHistory.builder()
                .appliance(saved)
                .fromStatus(from)
                .toStatus(ApplianceStatus.REVOKED)
                .actorId(actorId)
                .reason(reason)
                .build());

        eventPublisher.publish(EventTypes.AGGREGATE_APPLIANCE, saved.getId().toString(),
                EventTypes.APPLIANCE_REVOKED,
                new ApplianceRevokedEvent(saved.getId(), saved.getResidentId(),
                        reason, actorId, autoRevoked));
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Appliance getById(UUID id) {
        return applianceRepository.findById(id)
                .orElseThrow(() -> new ApplianceNotFoundException(
                        "Прибор не найден: id=" + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Appliance> search(ApplianceStatus status, UUID residentId, UUID userId,
                                  UUID roomId, ApplianceType type, String search, Pageable pageable) {
        return applianceRepository.findAll(
                ApplianceSpecifications.search(status, residentId, userId, roomId, type, search),
                pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appliance> listApprovedByRoom(UUID roomId) {
        return applianceRepository.findByRoomIdAndStatus(roomId, ApplianceStatus.APPROVED);
    }
}
