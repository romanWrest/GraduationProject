package ru.dstu.dormitory.reports_service.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.dstu.dormitory.reports_service.domain.enums.RequestStatus;
import ru.dstu.dormitory.reports_service.domain.enums.RequestType;
import ru.dstu.dormitory.reports_service.domain.model.RequestView;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface RequestViewRepository extends JpaRepository<RequestView, UUID> {

    @Query("""
            SELECT r FROM RequestView r
            WHERE r.createdAt >= :from AND r.createdAt < :to
              AND (:type IS NULL OR r.type = :type)
              AND (:status IS NULL OR r.status = :status)
              AND (:assigneeId IS NULL OR r.assigneeId = :assigneeId)
            ORDER BY r.createdAt DESC
            """)
    List<RequestView> findForReport(@Param("from") Instant from,
                                    @Param("to") Instant to,
                                    @Param("type") RequestType type,
                                    @Param("status") RequestStatus status,
                                    @Param("assigneeId") UUID assigneeId);

    @Modifying
    @Query("UPDATE RequestView r SET r.authorName = :name WHERE r.authorId = :userId AND (r.authorName IS NULL OR r.authorName <> :name)")
    int updateAuthorName(@Param("userId") UUID userId, @Param("name") String name);

    @Modifying
    @Query("UPDATE RequestView r SET r.assigneeName = :name WHERE r.assigneeId = :userId AND (r.assigneeName IS NULL OR r.assigneeName <> :name)")
    int updateAssigneeName(@Param("userId") UUID userId, @Param("name") String name);

    @Modifying
    @Query("UPDATE RequestView r SET r.roomNumber = :number WHERE r.roomId = :roomId AND (r.roomNumber IS NULL OR r.roomNumber <> :number)")
    int updateRoomNumber(@Param("roomId") UUID roomId, @Param("number") String number);
}
