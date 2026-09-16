package ru.dstu.dormitory.requests_service.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.dstu.dormitory.requests_service.aspect.annotation.LogMethod;
import ru.dstu.dormitory.requests_service.config.MinioProperties;
import ru.dstu.dormitory.requests_service.domain.enums.RequestStatus;
import ru.dstu.dormitory.requests_service.domain.model.Request;
import ru.dstu.dormitory.requests_service.domain.model.RequestAttachment;
import ru.dstu.dormitory.requests_service.domain.repo.RequestAttachmentRepository;
import ru.dstu.dormitory.requests_service.domain.repo.RequestRepository;
import ru.dstu.dormitory.requests_service.exception.*;
import ru.dstu.dormitory.requests_service.security.AccessControl;
import ru.dstu.dormitory.requests_service.security.UserPrincipal;
import ru.dstu.dormitory.requests_service.service.AttachmentService;
import ru.dstu.dormitory.requests_service.service.RequestService;
import ru.dstu.dormitory.requests_service.service.storage.MinioService;
import ru.dstu.dormitory.requests_service.util.FileValidation;
import ru.dstu.dormitory.requests_service.util.LogPatterns;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private final RequestAttachmentRepository attachmentRepository;
    private final RequestRepository requestRepository;
    private final MinioService minioService;
    private final MinioProperties minioProperties;
    private final AccessControl accessControl;

    @Override
    @LogMethod("Загрузка вложения")
    @Transactional
    public RequestAttachment upload(UUID requestId, MultipartFile file, UserPrincipal actor) {
        Request request = loadRequest(requestId);
        assertCanUpload(actor, request);

        FileValidation.validateSize(file.getSize(), minioProperties.getMaxFileSizeBytes());
        FileValidation.validateContentType(file.getContentType(), file.getOriginalFilename());

        UUID attachmentId = UUID.randomUUID();
        String sanitized = FileValidation.sanitizeFileName(file.getOriginalFilename());
        String objectKey = minioService.buildObjectKey(requestId, attachmentId, sanitized);

        boolean uploaded = false;
        try (InputStream is = file.getInputStream()) {
            minioService.upload(objectKey, is, file.getSize(),
                    file.getContentType() == null ? "application/octet-stream" : file.getContentType());
            uploaded = true;

            RequestAttachment entity = RequestAttachment.builder()
                    .id(attachmentId)
                    .request(request)
                    .objectKey(objectKey)
                    .originalName(sanitized)
                    .sizeBytes(file.getSize())
                    .contentType(file.getContentType() == null ? "application/octet-stream" : file.getContentType())
                    .uploadedBy(actor.userId())
                    .build();
            RequestAttachment saved = attachmentRepository.save(entity);

            log.info(LogPatterns.ATTACHMENT_UPLOADED, requestId, attachmentId, saved.getSizeBytes(), saved.getContentType());
            return saved;
        } catch (IOException ex) {
            if (uploaded) {
                minioService.delete(objectKey);
            }
            throw new StorageUnavailableException("Не удалось прочитать загруженный файл", ex);
        } catch (RuntimeException ex) {
            if (uploaded) {
                minioService.delete(objectKey);
            }
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public RequestAttachment getMetadata(UUID requestId, UUID attachmentId, UserPrincipal actor) {
        Request request = loadRequest(requestId);
        accessControl.requireCanView(actor, request);
        return loadOwned(requestId, attachmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public InputStream download(UUID requestId, UUID attachmentId, UserPrincipal actor) {
        RequestAttachment attachment = getMetadata(requestId, attachmentId, actor);
        return minioService.download(attachment.getObjectKey());
    }

    @Override
    @LogMethod("Удаление вложения")
    @Transactional
    public void delete(UUID requestId, UUID attachmentId, UserPrincipal actor) {
        Request request = loadRequest(requestId);
        if (!accessControl.isAuthor(actor, request) && !accessControl.isAdmin(actor)) {
            throw new ForbiddenActionException("Удалить вложение может только автор или ADMIN");
        }
        if (isAfterAssigned(request.getStatus())) {
            throw new AttachmentCannotBeDeletedException();
        }
        RequestAttachment attachment = loadOwned(requestId, attachmentId);
        attachmentRepository.delete(attachment);
        minioService.delete(attachment.getObjectKey());
        log.info(LogPatterns.ATTACHMENT_DELETED, requestId, attachmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestAttachment> list(UUID requestId, UserPrincipal actor) {
        Request request = loadRequest(requestId);
        accessControl.requireCanView(actor, request);
        return attachmentRepository.findByRequestIdOrderByUploadedAtAsc(requestId);
    }

    private RequestAttachment loadOwned(UUID requestId, UUID attachmentId) {
        RequestAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new AttachmentNotFoundException(attachmentId));
        if (!attachment.getRequest().getId().equals(requestId)) {
            throw new AttachmentNotFoundException(attachmentId);
        }
        return attachment;
    }

    private void assertCanUpload(UserPrincipal actor, Request request) {
        if (accessControl.isAdmin(actor)) {
            return;
        }
        if (accessControl.isAuthor(actor, request)) {
            if (isAfterAssigned(request.getStatus())) {
                throw new ForbiddenActionException("Автор может загружать вложения только до назначения исполнителя");
            }
            return;
        }
        if (accessControl.isAssignee(actor, request)) {
            return;
        }
        throw new ForbiddenActionException("Нет прав на загрузку вложений");
    }

    private boolean isAfterAssigned(RequestStatus status) {
        return status == RequestStatus.ASSIGNED
                || status == RequestStatus.IN_PROGRESS
                || status == RequestStatus.DONE
                || status == RequestStatus.CLOSED
                || status == RequestStatus.REJECTED
                || status == RequestStatus.CANCELLED;
    }

    private Request loadRequest(UUID requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException(requestId));
    }
}
