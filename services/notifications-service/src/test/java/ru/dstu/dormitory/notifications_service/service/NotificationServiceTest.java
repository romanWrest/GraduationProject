package ru.dstu.dormitory.notifications_service.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.dstu.dormitory.notifications_service.domain.enums.NotificationType;
import ru.dstu.dormitory.notifications_service.domain.model.Notification;
import ru.dstu.dormitory.notifications_service.domain.repo.NotificationRepository;
import ru.dstu.dormitory.notifications_service.exception.NotificationAccessDeniedException;
import ru.dstu.dormitory.notifications_service.exception.NotificationNotFoundException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    NotificationRepository repository;

    @InjectMocks
    NotificationService service;

    @Test
    void markRead_foreignNotification_throwsForbidden() {
        UUID notificationId = UUID.randomUUID();
        UUID owner = UUID.randomUUID();
        UUID intruder = UUID.randomUUID();

        Notification n = Notification.builder()
                .id(notificationId)
                .userId(owner)
                .type(NotificationType.WELCOME)
                .title("t").body("b")
                .createdAt(Instant.now())
                .build();
        when(repository.findById(notificationId)).thenReturn(Optional.of(n));

        assertThatThrownBy(() -> service.markRead(notificationId, intruder))
                .isInstanceOf(NotificationAccessDeniedException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void markRead_missingNotification_throwsNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.markRead(id, UUID.randomUUID()))
                .isInstanceOf(NotificationNotFoundException.class);
    }

    @Test
    void create_savesEntityWithFreshIdAndDefaults() {
        UUID userId = UUID.randomUUID();
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Notification saved = service.create(userId, NotificationType.WELCOME, "Hi", "Body", null);

        assert saved.getId() != null;
        assert saved.getCreatedAt() != null;
        assert !saved.isRead();
    }
}
