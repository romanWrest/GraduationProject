package ru.dstu.dormitory.reports_service.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.dstu.dormitory.reports_service.domain.model.ConsumableView;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface ConsumableViewRepository extends JpaRepository<ConsumableView, UUID> {

    @Query("""
            SELECT c FROM ConsumableView c
            WHERE c.issuedAt >= :from AND c.issuedAt < :to
              AND (:typeId IS NULL OR c.typeId = :typeId)
            ORDER BY c.issuedAt DESC
            """)
    List<ConsumableView> findForReport(@Param("from") Instant from,
                                        @Param("to") Instant to,
                                        @Param("typeId") UUID typeId);

    @Modifying
    @Query("UPDATE ConsumableView c SET c.residentName = :name WHERE c.userId = :userId AND (c.residentName IS NULL OR c.residentName <> :name)")
    int updateResidentNameByUserId(@Param("userId") UUID userId, @Param("name") String name);
}
