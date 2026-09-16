package ru.dstu.dormitory.notifications_service.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.dstu.dormitory.notifications_service.domain.model.ProcessedEvent;

import java.util.UUID;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {
}
