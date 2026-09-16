package ru.dstu.dormitory.notifications_service.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.dstu.dormitory.notifications_service.domain.model.EmailLog;

import java.util.UUID;

public interface EmailLogRepository extends JpaRepository<EmailLog, UUID> {
}
