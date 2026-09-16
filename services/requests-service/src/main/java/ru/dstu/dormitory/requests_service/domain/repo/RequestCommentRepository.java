package ru.dstu.dormitory.requests_service.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.dstu.dormitory.requests_service.domain.model.RequestComment;

import java.util.List;
import java.util.UUID;

@Repository
public interface RequestCommentRepository extends JpaRepository<RequestComment, UUID> {

    List<RequestComment> findByRequestIdOrderByCreatedAtAsc(UUID requestId);
}
