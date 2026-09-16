package ru.dstu.dormitory.requests_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import ru.dstu.dormitory.requests_service.config.MinioProperties;
import ru.dstu.dormitory.requests_service.domain.enums.RequestStatus;
import ru.dstu.dormitory.requests_service.domain.enums.RequestType;
import ru.dstu.dormitory.requests_service.domain.enums.RoleCode;
import ru.dstu.dormitory.requests_service.domain.model.Request;
import ru.dstu.dormitory.requests_service.domain.model.RequestAttachment;
import ru.dstu.dormitory.requests_service.domain.repo.RequestAttachmentRepository;
import ru.dstu.dormitory.requests_service.exception.AttachmentFormatNotAllowedException;
import ru.dstu.dormitory.requests_service.exception.AttachmentTooLargeException;
import ru.dstu.dormitory.requests_service.exception.StorageUnavailableException;
import ru.dstu.dormitory.requests_service.security.AccessControl;
import ru.dstu.dormitory.requests_service.security.UserPrincipal;
import ru.dstu.dormitory.requests_service.service.Impl.AttachmentServiceImpl;
import ru.dstu.dormitory.requests_service.service.storage.MinioService;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceImplTest {

    @Mock
    RequestAttachmentRepository attachmentRepository;
    @Mock
    RequestService requestService;
    @Mock
    MinioService minioService;

    MinioProperties minioProperties = new MinioProperties();
    AccessControl accessControl = new AccessControl();

    AttachmentServiceImpl service;

    UUID authorId = UUID.randomUUID();
    UUID requestId = UUID.randomUUID();
    UserPrincipal author;

    @BeforeEach
    void setUp() {
        minioProperties.setMaxFileSizeBytes(1024);
        minioProperties.setBucket("requests-attachments");
        service = new AttachmentServiceImpl(
                attachmentRepository, requestService, minioService, minioProperties, accessControl);
        author = new UserPrincipal(authorId, "a@b.c", Set.of(RoleCode.RESIDENT));
    }

    @Test
    void rejectsOversizedFile() {
        byte[] data = new byte[2048];
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", data);
        Request request = newRequest(RequestStatus.IN_REVIEW);
        when(requestService.getById(requestId)).thenReturn(request);

        assertThrows(AttachmentTooLargeException.class,
                () -> service.upload(requestId, file, author));
    }

    @Test
    void rejectsForbiddenFormat() {
        MockMultipartFile file = new MockMultipartFile("file", "virus.exe",
                "application/x-msdownload", "binary".getBytes());
        Request request = newRequest(RequestStatus.IN_REVIEW);
        when(requestService.getById(requestId)).thenReturn(request);

        assertThrows(AttachmentFormatNotAllowedException.class,
                () -> service.upload(requestId, file, author));
    }

    @Test
    void compensatesUploadOnDbFailure() {
        byte[] data = "pdf".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", data);

        Request request = newRequest(RequestStatus.IN_REVIEW);
        when(requestService.getById(requestId)).thenReturn(request);
        when(minioService.buildObjectKey(any(), any(), anyString())).thenReturn("key");
        doThrow(new RuntimeException("db down"))
                .when(attachmentRepository).save(any(RequestAttachment.class));

        assertThrows(RuntimeException.class, () -> service.upload(requestId, file, author));
        verify(minioService, atLeastOnce()).delete(anyString());
    }

    @Test
    void storageExceptionIsPropagated() {
        byte[] data = "pdf".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", data);

        Request request = newRequest(RequestStatus.IN_REVIEW);
        when(requestService.getById(requestId)).thenReturn(request);
        when(minioService.buildObjectKey(any(), any(), anyString())).thenReturn("key");
        doThrow(new StorageUnavailableException("MinIO down"))
                .when(minioService).upload(anyString(), any(), anyLong(), anyString());

        assertThrows(StorageUnavailableException.class, () -> service.upload(requestId, file, author));
    }

    private Request newRequest(RequestStatus status) {
        return Request.builder()
                .id(requestId)
                .authorId(authorId)
                .type(RequestType.OTHER)
                .status(status)
                .build();
    }
}
