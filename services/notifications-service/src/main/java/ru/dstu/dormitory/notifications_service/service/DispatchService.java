package ru.dstu.dormitory.notifications_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.dstu.dormitory.notifications_service.aspect.annotation.LogMethod;
import ru.dstu.dormitory.notifications_service.client.AuthFacade;
import ru.dstu.dormitory.notifications_service.client.dto.AuthUserDto;
import ru.dstu.dormitory.notifications_service.domain.enums.NotificationChannel;
import ru.dstu.dormitory.notifications_service.domain.enums.NotificationType;
import ru.dstu.dormitory.notifications_service.domain.model.Notification;
import ru.dstu.dormitory.notifications_service.util.LogPatterns;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Центральная диспетчеризация: получатель + каналы + настройки → in-app + email.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DispatchService {

    private final NotificationService notificationService;
    private final SettingsService settingsService;
    private final EmailService emailService;
    private final AuthFacade authFacade;

    /**
     * Доставить уведомление пользователю с учётом его настроек.
     *
     * @param subject          тема письма
     * @param renderedHtml     уже отрендеренное тело письма (если null — email не отправляется)
     * @param emailTemplate    имя шаблона (для email_log)
     */
    // DECISION: НЕ @Transactional. notificationService.create()/emailService.send()/recordSkip()
    // уже сами @Transactional. Если обернуть в общую TX, медленный SMTP внутри send() держит её
    // открытой, и сохранённое in-app уведомление становится невидимым для GET /notifications
    // (READ COMMITTED) до завершения отправки. Разносим коммиты — in-app публикуется сразу.
    @LogMethod(value = "Доставка уведомления", logArgs = {"userId", "type"})
    public Notification dispatch(UUID userId,
                                 NotificationType type,
                                 String title,
                                 String body,
                                 Map<String, Object> payload,
                                 String emailTemplate,
                                 String subject,
                                 String renderedHtml) {

        Notification saved = null;
        if (settingsService.isChannelEnabled(userId, type, NotificationChannel.IN_APP)) {
            saved = notificationService.create(userId, type, title, body, payload);
        }

        if (renderedHtml != null && emailTemplate != null) {
            if (settingsService.isChannelEnabled(userId, type, NotificationChannel.EMAIL)) {
                AuthUserDto user = authFacade.getUser(userId);
                if (user == null || user.email() == null || user.email().isBlank()) {
                    emailService.recordSkip(userId, user == null ? null : user.email(),
                            emailTemplate, subject, "auth unavailable or email missing");
                } else {
                    emailService.send(userId, user.email(), emailTemplate, subject, renderedHtml);
                }
            } else {
                log.info(LogPatterns.EMAIL_SKIPPED_BY_SETTINGS, userId, type);
            }
        }

        return saved;
    }

    /**
     * Прямая доставка через {@code InternalController}.
     */
    // DECISION: НЕ @Transactional — см. комментарий у dispatch(): синхронный SMTP не должен
    // удерживать TX, иначе in-app уведомление невидимо до завершения отправки письма.
    public Notification dispatchDirect(UUID userId,
                                       NotificationType type,
                                       String title,
                                       String body,
                                       Map<String, Object> payload,
                                       List<NotificationChannel> channels,
                                       String emailTemplate,
                                       String renderedHtml) {

        Notification saved = null;
        if (channels.contains(NotificationChannel.IN_APP)) {
            saved = notificationService.create(userId, type, title, body, payload);
        }

        if (channels.contains(NotificationChannel.EMAIL) && renderedHtml != null && emailTemplate != null) {
            AuthUserDto user = authFacade.getUser(userId);
            if (user == null || user.email() == null || user.email().isBlank()) {
                emailService.recordSkip(userId, user == null ? null : user.email(),
                        emailTemplate, title, "auth unavailable or email missing");
            } else {
                emailService.send(userId, user.email(), emailTemplate, title, renderedHtml);
            }
        }
        return saved;
    }
}
