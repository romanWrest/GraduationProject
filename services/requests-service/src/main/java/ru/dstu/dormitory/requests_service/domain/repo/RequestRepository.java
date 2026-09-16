package ru.dstu.dormitory.requests_service.domain.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.dstu.dormitory.requests_service.domain.enums.RequestStatus;
import ru.dstu.dormitory.requests_service.domain.enums.TargetPool;
import ru.dstu.dormitory.requests_service.domain.model.Request;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface RequestRepository extends JpaRepository<Request, UUID>, JpaSpecificationExecutor<Request> {

    Page<Request> findByTargetPoolAndStatus(TargetPool targetPool, RequestStatus status, Pageable pageable);

    @Query("""
            select r from Request r
            where r.status = :status
              and r.updatedAt < :threshold
            """)
    List<Request> findStaleByStatus(@Param("status") RequestStatus status,
                                    @Param("threshold") Instant threshold);

    @Query("""
            select r from Request r
            where r.assigneeId = :userId
              and r.status in :statuses
            """)
    List<Request> findByAssigneeAndStatuses(@Param("userId") UUID userId,
                                            @Param("statuses") List<RequestStatus> statuses);

    @Query("""
            select r from Request r
            where r.authorId = :userId
              and r.status in :statuses
            """)
    List<Request> findByAuthorAndStatuses(@Param("userId") UUID userId,
                                          @Param("statuses") List<RequestStatus> statuses);
}
