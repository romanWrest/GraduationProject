package ru.dstu.dormitory.residents_service.domain.repo;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.dstu.dormitory.residents_service.domain.model.Resident;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResidentRepository extends JpaRepository<Resident, UUID>, JpaSpecificationExecutor<Resident> {

    @EntityGraph(attributePaths = "room")
    Optional<Resident> findById(UUID id);

    @EntityGraph(attributePaths = "room")
    Optional<Resident> findByUserId(UUID userId);

    boolean existsByUserIdAndEvictedAtIsNull(UUID userId);

    @EntityGraph(attributePaths = "room")
    List<Resident> findAllByUserIdIn(List<UUID> userIds);

    @EntityGraph(attributePaths = "room")
    List<Resident> findAllByIdIn(List<UUID> ids);

    @Query("""
            select count(r) from Resident r
            where r.room.id = :roomId and r.evictedAt is null
            """)
    long countActiveByRoomId(@Param("roomId") UUID roomId);

    @EntityGraph(attributePaths = "room")
    List<Resident> findByRoomIdAndEvictedAtIsNull(UUID roomId);
}
