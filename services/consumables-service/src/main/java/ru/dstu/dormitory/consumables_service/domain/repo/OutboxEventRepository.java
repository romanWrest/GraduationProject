package ru.dstu.dormitory.consumables_service.domain.repo;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.dstu.dormitory.consumables_service.domain.model.OutboxEvent;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    @Query("select e from OutboxEvent e where e.sentAt is null order by e.createdAt asc")
    List<OutboxEvent> findUnsent(Pageable pageable);
}
