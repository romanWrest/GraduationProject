package ru.dstu.dormitory.notifications_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.notifications_service.aspect.annotation.LogMethod;
import ru.dstu.dormitory.notifications_service.domain.enums.NotificationType;
import ru.dstu.dormitory.notifications_service.domain.model.Notification;
import ru.dstu.dormitory.notifications_service.domain.repo.NotificationRepository;
import ru.dstu.dormitory.notifications_service.exception.NotificationAccessDeniedException;
import ru.dstu.dormitory.notifications_service.exception.NotificationNotFoundException;
import ru.dstu.dormitory.notifications_service.util.LogPatterns;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * In-app уведомления: создание, поиск, отметка прочитанных.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;

    /** Сохранить новое уведомление. Используется как Kafka-handler-ами, так и InternalController. */
    @Transactional
    @LogMethod(value = "Создание in-app уведомления", logArgs = {"userId", "type"})
    public Notification create(UUID userId, NotificationType type, String title, String body,
                               Map<String, Object> payload) {
        Notification n = Notification.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .type(type)
                .title(title)
                .body(body)
                .payload(payload)
                .read(false)
                .createdAt(Instant.now())
                .build();
        Notification saved = repository.save(n);
        log.info(LogPatterns.NOTIFICATION_CREATED, userId, type);
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<Notification> search(UUID userId, boolean unreadOnly, NotificationType type,
                                     Instant from, Instant to, Pageable pageable) {
        return repository.search(userId, unreadOnly, type, from, to, pageable);
    }

    @Transactional(readOnly = true)
    public long countUnread(UUID userId) {
        return repository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    @LogMethod(value = "Пометить уведомление прочитанным", logArgs = {"id", "userId"})
    public void markRead(UUID id, UUID userId) {
        Notification n = repository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(id));
        if (!n.getUserId().equals(userId)) {
            throw new NotificationAccessDeniedException();
        }
        if (!n.isRead()) {
            n.setRead(true);
            n.setReadAt(Instant.now());
            repository.save(n);
            log.info(LogPatterns.NOTIFICATION_READ, id, userId);
        }
    }

    @Transactional
    @LogMethod(value = "Пометить все уведомления прочитанными", logArgs = {"userId"})
    public int markAllRead(UUID userId) {
        return repository.markAllRead(userId, Instant.now());
    }
}
