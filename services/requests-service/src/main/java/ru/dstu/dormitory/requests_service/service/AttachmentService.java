package ru.dstu.dormitory.requests_service.service;

import org.springframework.web.multipart.MultipartFile;
import ru.dstu.dormitory.requests_service.domain.model.RequestAttachment;
import ru.dstu.dormitory.requests_service.security.UserPrincipal;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface AttachmentService {

    RequestAttachment upload(UUID requestId, MultipartFile file, UserPrincipal actor);

    RequestAttachment getMetadata(UUID requestId, UUID attachmentId, UserPrincipal actor);

    InputStream download(UUID requestId, UUID attachmentId, UserPrincipal actor);

    void delete(UUID requestId, UUID attachmentId, UserPrincipal actor);

    List<RequestAttachment> list(UUID requestId, UserPrincipal actor);
}
