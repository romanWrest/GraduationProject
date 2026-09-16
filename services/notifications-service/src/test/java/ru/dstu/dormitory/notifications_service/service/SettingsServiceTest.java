package ru.dstu.dormitory.notifications_service.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.dstu.dormitory.notifications_service.domain.enums.NotificationChannel;
import ru.dstu.dormitory.notifications_service.domain.enums.NotificationType;
import ru.dstu.dormitory.notifications_service.domain.model.NotificationSettings;
import ru.dstu.dormitory.notifications_service.domain.repo.NotificationSettingsRepository;
import ru.dstu.dormitory.notifications_service.exception.InvalidNotificationSettingsException;
import ru.dstu.dormitory.notifications_service.web.dto.UpdateSettingsDto;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettingsServiceTest {

    @Mock
    NotificationSettingsRepository repository;

    @InjectMocks
    SettingsService service;

    @Test
    void defaultSettings_bothChannelsEnabled() {
        UUID userId = UUID.randomUUID();
        when(repository.findById(userId)).thenReturn(Optional.empty());

        boolean email = service.isChannelEnabled(userId, NotificationType.WELCOME, NotificationChannel.EMAIL);
        boolean inApp = service.isChannelEnabled(userId, NotificationType.WELCOME, NotificationChannel.IN_APP);

        assertThat(email).isTrue();
        assertThat(inApp).isTrue();
    }

    @Test
    void emailDisabledGlobally_returnsFalseForEmail() {
        UUID userId = UUID.randomUUID();
        NotificationSettings settings = NotificationSettings.builder()
                .userId(userId)
                .emailEnabled(false)
                .inAppEnabled(true)
                .byType(new HashMap<>())
                .updatedAt(Instant.now())
                .build();
        when(repository.findById(userId)).thenReturn(Optional.of(settings));

        assertThat(service.isChannelEnabled(userId, NotificationType.WELCOME, NotificationChannel.EMAIL))
                .isFalse();
        assertThat(service.isChannelEnabled(userId, NotificationType.WELCOME, NotificationChannel.IN_APP))
                .isTrue();
    }

    @Test
    void perTypeOverrideRespected() {
        UUID userId = UUID.randomUUID();
        Map<String, Map<String, Boolean>> byType = new HashMap<>();
        byType.put("REQUEST_CREATED", Map.of("email", false, "inApp", true));
        NotificationSettings settings = NotificationSettings.builder()
                .userId(userId)
                .emailEnabled(true)
                .inAppEnabled(true)
                .byType(byType)
                .updatedAt(Instant.now())
                .build();
        when(repository.findById(userId)).thenReturn(Optional.of(settings));

        assertThat(service.isChannelEnabled(userId, NotificationType.REQUEST_CREATED, NotificationChannel.EMAIL))
                .isFalse();
        assertThat(service.isChannelEnabled(userId, NotificationType.REQUEST_CREATED, NotificationChannel.IN_APP))
                .isTrue();
        // Другой тип уведомления — без override
        assertThat(service.isChannelEnabled(userId, NotificationType.WELCOME, NotificationChannel.EMAIL))
                .isTrue();
    }

    @Test
    void invalidByTypeKeyRejected() {
        UUID userId = UUID.randomUUID();
        UpdateSettingsDto dto = new UpdateSettingsDto(null, null, Map.of(
                "DOES_NOT_EXIST", Map.of("email", true)));

        assertThatThrownBy(() -> service.update(userId, dto))
                .isInstanceOf(InvalidNotificationSettingsException.class);
    }
}
