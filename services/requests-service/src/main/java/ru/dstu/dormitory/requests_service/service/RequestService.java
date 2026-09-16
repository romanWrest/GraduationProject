package ru.dstu.dormitory.requests_service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import ru.dstu.dormitory.requests_service.domain.enums.RequestStatus;
import ru.dstu.dormitory.requests_service.domain.enums.RequestType;
import ru.dstu.dormitory.requests_service.domain.model.Request;
import ru.dstu.dormitory.requests_service.domain.model.RequestStatusHistory;
import ru.dstu.dormitory.requests_service.security.UserPrincipal;
import ru.dstu.dormitory.requests_service.web.dto.CreateRequestDto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface RequestService {

    Request create(CreateRequestDto dto, UserPrincipal actor, List<MultipartFile> files);

    Request getById(UUID id);

    Page<Request> search(UserPrincipal actor,
                         RequestType type,
                         RequestStatus status,
                         UUID assigneeId,
                         UUID authorId,
                         UUID roomId,
                         Instant from,
                         Instant to,
                         String search,
                         Pageable pageable);

    Page<Request> pool(UserPrincipal actor, Pageable pageable);

    Request review(UUID id, UserPrincipal actor);

    Request assign(UUID id, UUID assigneeId, Instant scheduledAt, UserPrincipal actor);

    Request start(UUID id, UserPrincipal actor);

    Request done(UUID id, String resolutionComment, UserPrincipal actor);

    Request confirm(UUID id, UserPrincipal actor);

    Request reject(UUID id, String reason, UserPrincipal actor);

    Request cancel(UUID id, String reason, UserPrincipal actor);

    Request reopen(UUID id, String reason, UserPrincipal actor);

    List<RequestStatusHistory> history(UUID id, UserPrincipal actor);
}
