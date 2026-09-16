package ru.dstu.dormitory.notifications_service.domain.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.dstu.dormitory.notifications_service.domain.enums.NotificationType;
import ru.dstu.dormitory.notifications_service.domain.model.Notification;

import java.time.Instant;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    // было
//    @Query("""
//            SELECT n FROM Notification n
//            WHERE n.userId = :userId
//              AND (:unreadOnly = false OR n.read = false)
//              AND (:type IS NULL OR n.type = :type)
//              AND (:from IS NULL OR n.createdAt >= :from)
//              AND (:to IS NULL OR n.createdAt <= :to)
//            ORDER BY n.createdAt DESC
//            """)
//    Page<Notification> search(@Param("userId") UUID userId,
//                              @Param("unreadOnly") boolean unreadOnly,
//                              @Param("type") NotificationType type,
//                              @Param("from") Instant from,
//                              @Param("to") Instant to,
//                              Pageable pageable);

    @Query("""
        SELECT n FROM Notification n
        WHERE n.userId = :userId
          AND (:unreadOnly = false OR n.read = false)
          AND (cast(:type as string) IS NULL OR n.type = :type)
          AND (cast(:from as timestamp) IS NULL OR n.createdAt >= :from)
          AND (cast(:to   as timestamp) IS NULL OR n.createdAt <= :to)
        ORDER BY n.createdAt DESC
        """)
    Page<Notification> search(@Param("userId") UUID userId,
                              @Param("unreadOnly") boolean unreadOnly,
                              @Param("type") NotificationType type,
                              @Param("from") Instant from,
                              @Param("to") Instant to,
                              Pageable pageable);
    long countByUserIdAndReadFalse(UUID userId);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true, n.readAt = :now WHERE n.userId = :userId AND n.read = false")
    int markAllRead(@Param("userId") UUID userId, @Param("now") Instant now);
}
