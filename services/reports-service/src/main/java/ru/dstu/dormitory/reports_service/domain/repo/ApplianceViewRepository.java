package ru.dstu.dormitory.reports_service.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.dstu.dormitory.reports_service.domain.enums.ApplianceStatus;
import ru.dstu.dormitory.reports_service.domain.model.ApplianceView;

import java.util.List;
import java.util.UUID;

@Repository
public interface ApplianceViewRepository extends JpaRepository<ApplianceView, UUID> {

    @Query("""
            SELECT a FROM ApplianceView a
            WHERE (:roomId IS NULL OR a.roomId = :roomId)
              AND (:status IS NULL OR a.status = :status)
            ORDER BY a.roomNumber, a.type
            """)
    List<ApplianceView> findForReport(@Param("roomId") UUID roomId,
                                       @Param("status") ApplianceStatus status);

    @Modifying
    @Query("UPDATE ApplianceView a SET a.residentName = :name WHERE a.userId = :userId AND (a.residentName IS NULL OR a.residentName <> :name)")
    int updateResidentNameByUserId(@Param("userId") UUID userId, @Param("name") String name);
}
