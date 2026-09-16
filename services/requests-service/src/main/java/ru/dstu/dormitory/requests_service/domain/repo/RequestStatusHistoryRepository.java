package ru.dstu.dormitory.requests_service.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.dstu.dormitory.requests_service.domain.model.RequestStatusHistory;

import java.util.List;
import java.util.UUID;

@Repository
public interface RequestStatusHistoryRepository extends JpaRepository<RequestStatusHistory, UUID> {

    List<RequestStatusHistory> findByRequestIdOrderByChangedAtAsc(UUID requestId);
}
