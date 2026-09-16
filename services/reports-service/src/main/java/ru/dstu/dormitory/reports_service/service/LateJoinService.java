package ru.dstu.dormitory.reports_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.reports_service.domain.repo.ApplianceViewRepository;
import ru.dstu.dormitory.reports_service.domain.repo.ConsumableViewRepository;
import ru.dstu.dormitory.reports_service.domain.repo.RequestViewRepository;
import ru.dstu.dormitory.reports_service.domain.repo.ResidentViewRepository;
import ru.dstu.dormitory.reports_service.util.LogPatterns;

import java.util.UUID;

/**
 * Поздний JOIN: при появлении/обновлении users_view проходим по другим read-моделям
 * и заполняем поля с именем, где было null. Eventual consistency.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LateJoinService {

    private final RequestViewRepository requestRepo;
    private final ResidentViewRepository residentRepo;
    private final ApplianceViewRepository applianceRepo;
    private final ConsumableViewRepository consumableRepo;

    @Transactional
    public void propagateUserName(UUID userId, String fullName) {
        if (userId == null || fullName == null || fullName.isBlank()) {
            return;
        }
        int requestsAuthor = requestRepo.updateAuthorName(userId, fullName);
        int requestsAssignee = requestRepo.updateAssigneeName(userId, fullName);
        int residents = residentRepo.updateFullNameByUserId(userId, fullName);
        int appliances = applianceRepo.updateResidentNameByUserId(userId, fullName);
        int consumables = consumableRepo.updateResidentNameByUserId(userId, fullName);

        if (requestsAuthor + requestsAssignee + residents + appliances + consumables > 0) {
            log.debug(LogPatterns.LATE_JOIN_APPLIED, "users_view→*",
                    requestsAuthor + requestsAssignee + residents + appliances + consumables);
        }
    }
}
