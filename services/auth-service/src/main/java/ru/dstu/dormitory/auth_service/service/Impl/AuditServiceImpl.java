package ru.dstu.dormitory.auth_service.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.auth_service.domain.model.AuditLog;
import ru.dstu.dormitory.auth_service.domain.repo.AuditLogRepository;
import ru.dstu.dormitory.auth_service.service.AuditService;
import ru.dstu.dormitory.auth_service.util.LogPatterns;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository repository;

    @Override
    @Async("auditExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(UUID actorId, String action, String targetType, String targetId, Map<String, Object> meta) {
        try {
            AuditLog entry = AuditLog.builder()
                    .actorId(actorId)
                    .action(action)
                    .targetType(targetType)
                    .targetId(targetId)
                    .meta(meta)
                    .build();
            repository.save(entry);
            log.info(LogPatterns.AUDIT_RECORDED, action, actorId, targetId);
        } catch (Exception ex) {
            log.error(LogPatterns.AUDIT_FAILED, action, ex.getMessage());
        }
    }
}
