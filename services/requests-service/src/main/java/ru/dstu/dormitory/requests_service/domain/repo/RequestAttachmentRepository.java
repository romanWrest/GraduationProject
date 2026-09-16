package ru.dstu.dormitory.requests_service.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.dstu.dormitory.requests_service.domain.model.RequestAttachment;

import java.util.List;
import java.util.UUID;

@Repository
public interface RequestAttachmentRepository extends JpaRepository<RequestAttachment, UUID> {

    List<RequestAttachment> findByRequestIdOrderByUploadedAtAsc(UUID requestId);
}
