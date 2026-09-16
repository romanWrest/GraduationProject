package ru.dstu.dormitory.reports_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.reports_service.domain.model.ProcessedEvent;
import ru.dstu.dormitory.reports_service.domain.repo.ProcessedEventRepository;
import ru.dstu.dormitory.reports_service.util.LogPatterns;

import java.time.Instant;
import java.util.UUID;

/**
 * Запись и проверка идемпотентности по {@code eventId}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final ProcessedEventRepository repository;

    @Transactional(readOnly = true)
    public boolean isProcessed(UUID eventId) {
        if (eventId == null) {
            return false;
        }
        return repository.existsById(eventId);
    }

    /**
     * Зафиксировать обработку события. Возвращает true, если запись добавлена;
     * false — если уже была (race condition).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean markProcessed(UUID eventId, String eventType, String topic) {
        if (eventId == null) {
            return false;
        }
        try {
            repository.save(ProcessedEvent.builder()
                    .eventId(eventId)
                    .eventType(eventType)
                    .topic(topic)
                    .processedAt(Instant.now())
                    .build());
            return true;
        } catch (DataIntegrityViolationException ex) {
            log.info(LogPatterns.EVENT_DUPLICATE, eventId);
            return false;
        }
    }
}
