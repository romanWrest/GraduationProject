package ru.dstu.dormitory.residents_service.domain.repo;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.dstu.dormitory.residents_service.domain.model.ResidencyHistory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResidencyHistoryRepository extends JpaRepository<ResidencyHistory, UUID> {

    @EntityGraph(attributePaths = {"resident", "room"})
    List<ResidencyHistory> findByResidentIdOrderByMovedInAtAsc(UUID residentId);

    @EntityGraph(attributePaths = {"resident", "room"})
    List<ResidencyHistory> findByRoomIdOrderByMovedInAtAsc(UUID roomId);

    Optional<ResidencyHistory> findFirstByResidentIdAndMovedOutAtIsNullOrderByMovedInAtDesc(UUID residentId);
}
