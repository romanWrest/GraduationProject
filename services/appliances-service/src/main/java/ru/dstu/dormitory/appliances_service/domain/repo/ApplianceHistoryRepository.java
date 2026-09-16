package ru.dstu.dormitory.appliances_service.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.dstu.dormitory.appliances_service.domain.model.ApplianceHistory;

import java.util.UUID;

@Repository
public interface ApplianceHistoryRepository extends JpaRepository<ApplianceHistory, UUID> {
}
