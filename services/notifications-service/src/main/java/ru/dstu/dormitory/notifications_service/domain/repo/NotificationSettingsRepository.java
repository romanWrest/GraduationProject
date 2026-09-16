package ru.dstu.dormitory.notifications_service.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.dstu.dormitory.notifications_service.domain.model.NotificationSettings;

import java.util.UUID;

public interface NotificationSettingsRepository extends JpaRepository<NotificationSettings, UUID> {
}
