package ru.dstu.dormitory.consumables_service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.dstu.dormitory.consumables_service.domain.enums.IssueStatus;
import ru.dstu.dormitory.consumables_service.domain.model.ConsumableIssue;
import ru.dstu.dormitory.consumables_service.web.dto.request.IssueRequest;
import ru.dstu.dormitory.consumables_service.web.dto.request.ReturnRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ConsumableIssueService {

    /**
     * Создать выдачу: уменьшает stock соответствующего типа, фиксирует движение,
     * публикует событие {@code ConsumableIssued}.
     *
     * @param request данные выдачи
     * @param actorId инициатор операции (PROPERTY_MANAGER / ADMIN)
     */
    ConsumableIssue issue(IssueRequest request, UUID actorId);

    /**
     * Принять возврат. Если condition = OK или DAMAGED — увеличивает stock,
     * если LOST — нет. Публикует {@code ConsumableReturned}.
     */
    ConsumableIssue returnIssue(UUID issueId, ReturnRequest request, UUID actorId);

    ConsumableIssue getById(UUID id);

    Page<ConsumableIssue> search(UUID residentId, UUID typeId, IssueStatus status,
                                 Instant from, Instant to, Pageable pageable);

    List<ConsumableIssue> listIssuedByResident(UUID residentId);

    List<ConsumableIssue> listIssuedByUser(UUID userId);
}
