package ru.dstu.dormitory.reports_service.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.dstu.dormitory.reports_service.domain.model.ResidentView;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResidentViewRepository extends JpaRepository<ResidentView, UUID> {

    Optional<ResidentView> findFirstByUserIdAndEvictedAtIsNull(UUID userId);

    @Query("""
            SELECT r FROM ResidentView r
            WHERE r.enrolledAt <= :asOf
              AND (r.evictedAt IS NULL OR r.evictedAt > :asOf)
            ORDER BY r.fullName
            """)
    List<ResidentView> findActiveAsOf(@Param("asOf") LocalDate asOf);

    @Modifying
    @Query("UPDATE ResidentView r SET r.fullName = :name WHERE r.userId = :userId AND (r.fullName IS NULL OR r.fullName <> :name)")
    int updateFullNameByUserId(@Param("userId") UUID userId, @Param("name") String name);
}
