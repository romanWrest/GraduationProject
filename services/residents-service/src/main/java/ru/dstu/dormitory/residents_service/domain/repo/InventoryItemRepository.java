package ru.dstu.dormitory.residents_service.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.dstu.dormitory.residents_service.domain.model.InventoryItem;

import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, UUID> {

    List<InventoryItem> findByRoomIdOrderByCreatedAtAsc(UUID roomId);
}
