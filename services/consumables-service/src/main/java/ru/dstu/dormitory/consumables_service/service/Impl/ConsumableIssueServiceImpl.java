package ru.dstu.dormitory.consumables_service.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.consumables_service.aspect.annotation.LogMethod;
import ru.dstu.dormitory.consumables_service.client.ResidentsFacade;
import ru.dstu.dormitory.consumables_service.client.dto.ResidentDto;
import ru.dstu.dormitory.consumables_service.domain.enums.IssueStatus;
import ru.dstu.dormitory.consumables_service.domain.enums.ReturnCondition;
import ru.dstu.dormitory.consumables_service.domain.model.ConsumableIssue;
import ru.dstu.dormitory.consumables_service.domain.model.ConsumableType;
import ru.dstu.dormitory.consumables_service.domain.repo.ConsumableIssueRepository;
import ru.dstu.dormitory.consumables_service.domain.repo.ConsumableIssueSpecifications;
import ru.dstu.dormitory.consumables_service.exception.ConsumableIssueNotFoundException;
import ru.dstu.dormitory.consumables_service.exception.IssueAlreadyReturnedException;
import ru.dstu.dormitory.consumables_service.exception.IssueNotIssuedException;
import ru.dstu.dormitory.consumables_service.service.ConsumableIssueService;
import ru.dstu.dormitory.consumables_service.service.StockService;
import ru.dstu.dormitory.consumables_service.service.event.ConsumableIssuedEvent;
import ru.dstu.dormitory.consumables_service.service.event.ConsumableReturnedEvent;
import ru.dstu.dormitory.consumables_service.service.event.EventPublisher;
import ru.dstu.dormitory.consumables_service.service.event.EventTypes;
import ru.dstu.dormitory.consumables_service.util.LogPatterns;
import ru.dstu.dormitory.consumables_service.web.dto.request.IssueRequest;
import ru.dstu.dormitory.consumables_service.web.dto.request.ReturnRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumableIssueServiceImpl implements ConsumableIssueService {

    private static final String REASON_ISSUE = "Выдача жильцу";
    private static final String REASON_RETURN = "Возврат от жильца";

    private final ConsumableIssueRepository issueRepository;
    private final StockService stockService;
    private final ResidentsFacade residentsFacade;
    private final EventPublisher eventPublisher;

    @Override
    @LogMethod
    @Transactional
    public ConsumableIssue issue(IssueRequest request, UUID actorId) {
        ResidentDto resident = residentsFacade.requireResident(request.residentId());

        ConsumableType type = stockService.adjustStock(
                request.typeId(),
                -request.quantity(),
                REASON_ISSUE,
                actorId);

        ConsumableIssue issue = ConsumableIssue.builder()
                .residentId(resident.id())
                .userId(resident.userId())
                .type(type)
                .quantity(request.quantity())
                .status(IssueStatus.ISSUED)
                .issuedBy(actorId)
                .notes(request.notes())
                .build();
        ConsumableIssue saved = issueRepository.save(issue);

        log.info(LogPatterns.ISSUE_CREATED, saved.getId(), saved.getResidentId(),
                type.getId(), saved.getQuantity());

        eventPublisher.publish(EventTypes.AGGREGATE_ISSUE, saved.getId().toString(),
                EventTypes.CONSUMABLE_ISSUED,
                new ConsumableIssuedEvent(saved.getId(), saved.getResidentId(),
                        saved.getUserId(), type.getId(), type.getName(),
                        saved.getQuantity(), actorId));

        return saved;
    }

    @Override
    @LogMethod
    @Transactional
    public ConsumableIssue returnIssue(UUID issueId, ReturnRequest request, UUID actorId) {
        ConsumableIssue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ConsumableIssueNotFoundException(
                        "Выдача не найдена: id=" + issueId));

        if (issue.getStatus() == IssueStatus.RETURNED) {
            throw new IssueAlreadyReturnedException(
                    "Выдача уже возвращена: id=" + issueId);
        }
        if (issue.getStatus() != IssueStatus.ISSUED) {
            throw new IssueNotIssuedException(
                    "Выдача не в статусе ISSUED: id=%s, status=%s"
                            .formatted(issueId, issue.getStatus()));
        }

        ReturnCondition condition = request.condition();
        if (condition == ReturnCondition.OK || condition == ReturnCondition.DAMAGED) {
            stockService.adjustStock(issue.getType().getId(), issue.getQuantity(),
                    REASON_RETURN + " (" + condition + ")", actorId);
        }

        issue.setStatus(IssueStatus.RETURNED);
        issue.setReturnedAt(Instant.now());
        issue.setReturnedBy(actorId);
        issue.setReturnCondition(condition);
        if (request.notes() != null) {
            issue.setNotes(request.notes());
        }
        ConsumableIssue saved = issueRepository.save(issue);

        log.info(LogPatterns.ISSUE_RETURNED, saved.getId(), condition);

        eventPublisher.publish(EventTypes.AGGREGATE_ISSUE, saved.getId().toString(),
                EventTypes.CONSUMABLE_RETURNED,
                new ConsumableReturnedEvent(saved.getId(), saved.getResidentId(),
                        saved.getUserId(), saved.getType().getId(),
                        condition, actorId));

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public ConsumableIssue getById(UUID id) {
        return issueRepository.findById(id)
                .orElseThrow(() -> new ConsumableIssueNotFoundException(
                        "Выдача не найдена: id=" + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConsumableIssue> search(UUID residentId, UUID typeId, IssueStatus status,
                                        Instant from, Instant to, Pageable pageable) {
        return issueRepository.findAll(
                ConsumableIssueSpecifications.search(residentId, typeId, status, from, to),
                pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsumableIssue> listIssuedByResident(UUID residentId) {
        return issueRepository.findByResidentIdAndStatus(residentId, IssueStatus.ISSUED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsumableIssue> listIssuedByUser(UUID userId) {
        return issueRepository.findByUserIdAndStatus(userId, IssueStatus.ISSUED);
    }
}
