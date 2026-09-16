package ru.dstu.dormitory.notifications_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.notifications_service.aspect.annotation.LogMethod;
import ru.dstu.dormitory.notifications_service.domain.enums.NotificationChannel;
import ru.dstu.dormitory.notifications_service.domain.enums.NotificationType;
import ru.dstu.dormitory.notifications_service.domain.model.NotificationSettings;
import ru.dstu.dormitory.notifications_service.domain.repo.NotificationSettingsRepository;
import ru.dstu.dormitory.notifications_service.exception.InvalidNotificationSettingsException;
import ru.dstu.dormitory.notifications_service.web.dto.UpdateSettingsDto;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Настройки каналов доставки уведомлений на пользователя.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SettingsService {

    private static final String KEY_EMAIL = "email";
    private static final String KEY_IN_APP = "inApp";

    private final NotificationSettingsRepository repository;

    @Transactional
    public NotificationSettings getOrDefault(UUID userId) {
        return repository.findById(userId)
                .orElseGet(() -> repository.save(NotificationSettings.defaultsFor(userId)));
    }

    @Transactional
    @LogMethod(value = "Обновление настроек уведомлений", logArgs = {"userId"})
    public NotificationSettings update(UUID userId, UpdateSettingsDto dto) {
        validate(dto);
        NotificationSettings s = getOrDefault(userId);
        if (dto.emailEnabled() != null) {
            s.setEmailEnabled(dto.emailEnabled());
        }
        if (dto.inAppEnabled() != null) {
            s.setInAppEnabled(dto.inAppEnabled());
        }
        if (dto.byType() != null) {
            s.setByType(new HashMap<>(dto.byType()));
        }
        s.setUpdatedAt(Instant.now());
        return repository.save(s);
    }

    /**
     * Проверить, разрешён ли заданный канал для типа уведомления.
     * Если запись для пользователя отсутствует — возвращает {@code true} (поведение по умолчанию).
     */
    public boolean isChannelEnabled(UUID userId, NotificationType type, NotificationChannel channel) {
        NotificationSettings s = repository.findById(userId).orElse(null);
        if (s == null) {
            return true;
        }
        boolean globalEnabled = channel == NotificationChannel.EMAIL ? s.isEmailEnabled() : s.isInAppEnabled();
        if (!globalEnabled) {
            return false;
        }
        Map<String, Boolean> typeMap = s.getByType() == null ? null : s.getByType().get(type.name());
        if (typeMap == null) {
            return true;
        }
        String key = channel == NotificationChannel.EMAIL ? KEY_EMAIL : KEY_IN_APP;
        Boolean override = typeMap.get(key);
        return override == null || override;
    }

    private void validate(UpdateSettingsDto dto) {
        if (dto == null) {
            throw new InvalidNotificationSettingsException("Тело запроса не должно быть пустым");
        }
        if (dto.byType() != null) {
            for (Map.Entry<String, Map<String, Boolean>> e : dto.byType().entrySet()) {
                try {
                    NotificationType.valueOf(e.getKey());
                } catch (IllegalArgumentException ex) {
                    throw new InvalidNotificationSettingsException(
                            "Неизвестный тип уведомления: %s".formatted(e.getKey()));
                }
                if (e.getValue() == null) {
                    throw new InvalidNotificationSettingsException(
                            "Пустые настройки для типа %s".formatted(e.getKey()));
                }
            }
        }
    }
}
