package ru.dstu.dormitory.notifications_service.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.dstu.dormitory.notifications_service.client.AuthFacade;
import ru.dstu.dormitory.notifications_service.client.dto.AuthUserDto;
import ru.dstu.dormitory.notifications_service.domain.enums.NotificationChannel;
import ru.dstu.dormitory.notifications_service.domain.enums.NotificationType;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DispatchServiceTest {

    @Mock NotificationService notificationService;
    @Mock SettingsService settingsService;
    @Mock EmailService emailService;
    @Mock AuthFacade authFacade;

    @InjectMocks
    DispatchService dispatchService;

    @Test
    void emailDisabledByUser_noEmailSent_inAppCreated() {
        UUID userId = UUID.randomUUID();
        when(settingsService.isChannelEnabled(userId, NotificationType.REQUEST_CREATED, NotificationChannel.IN_APP))
                .thenReturn(true);
        when(settingsService.isChannelEnabled(userId, NotificationType.REQUEST_CREATED, NotificationChannel.EMAIL))
                .thenReturn(false);

        dispatchService.dispatch(userId, NotificationType.REQUEST_CREATED, "title", "body", Map.of(),
                "request-created", "subj", "<html/>");

        verify(notificationService).create(eq(userId), eq(NotificationType.REQUEST_CREATED),
                eq("title"), eq("body"), any());
        verify(emailService, never()).send(any(), any(), any(), any(), any());
    }

    @Test
    void emailEnabledButAuthDown_recordsSkip() {
        UUID userId = UUID.randomUUID();
        when(settingsService.isChannelEnabled(userId, NotificationType.WELCOME, NotificationChannel.IN_APP))
                .thenReturn(true);
        when(settingsService.isChannelEnabled(userId, NotificationType.WELCOME, NotificationChannel.EMAIL))
                .thenReturn(true);
        when(authFacade.getUser(userId)).thenReturn(null);

        dispatchService.dispatch(userId, NotificationType.WELCOME, "Hi", "Body", Map.of(),
                "welcome", "Welcome", "<html/>");

        verify(emailService).recordSkip(eq(userId), eq(null), eq("welcome"), eq("Welcome"), anyString());
        verify(emailService, never()).send(any(), any(), any(), any(), any());
    }

    @Test
    void emailAndInAppEnabled_bothInvoked() {
        UUID userId = UUID.randomUUID();
        AuthUserDto user = new AuthUserDto(userId, "u@example.com", "Иван", "+7",
                true, Set.of(), Instant.now(), Instant.now());
        when(settingsService.isChannelEnabled(userId, NotificationType.WELCOME, NotificationChannel.IN_APP))
                .thenReturn(true);
        when(settingsService.isChannelEnabled(userId, NotificationType.WELCOME, NotificationChannel.EMAIL))
                .thenReturn(true);
        when(authFacade.getUser(userId)).thenReturn(user);

        dispatchService.dispatch(userId, NotificationType.WELCOME, "Hi", "Body", Map.of(),
                "welcome", "Welcome", "<html/>");

        verify(notificationService, times(1)).create(any(), any(), any(), any(), any());
        verify(emailService, times(1)).send(eq(userId), eq("u@example.com"), eq("welcome"),
                eq("Welcome"), eq("<html/>"));
    }
}
