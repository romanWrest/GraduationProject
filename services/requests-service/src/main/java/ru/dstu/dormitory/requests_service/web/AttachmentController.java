package ru.dstu.dormitory.requests_service.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.dstu.dormitory.requests_service.domain.model.RequestAttachment;
import ru.dstu.dormitory.requests_service.mapper.AttachmentMapper;
import ru.dstu.dormitory.requests_service.security.CurrentUser;
import ru.dstu.dormitory.requests_service.security.UserPrincipal;
import ru.dstu.dormitory.requests_service.service.AttachmentService;
import ru.dstu.dormitory.requests_service.web.dto.AttachmentDto;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Tag(name = "Attachments", description = "Вложения к заявкам (MinIO)")
@RestController
@RequestMapping("/api/v1/requests/{id}/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;
    private final AttachmentMapper attachmentMapper;

    @Operation(summary = "Загрузить вложение")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentDto> upload(@PathVariable UUID id,
                                                @RequestParam("file") MultipartFile file,
                                                @CurrentUser UserPrincipal actor) {
        RequestAttachment saved = attachmentService.upload(id, file, actor);
        return ResponseEntity.ok(attachmentMapper.toDto(saved));
    }

    @Operation(summary = "Скачать вложение")
    @GetMapping("/{attId}")
    public ResponseEntity<InputStreamResource> download(@PathVariable UUID id,
                                                        @PathVariable UUID attId,
                                                        @CurrentUser UserPrincipal actor) {
        RequestAttachment attachment = attachmentService.getMetadata(id, attId, actor);
        InputStream stream = attachmentService.download(id, attId, actor);
        String fileName = URLEncoder.encode(attachment.getOriginalName(), StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''%s".formatted(fileName))
                .contentLength(attachment.getSizeBytes())
                .body(new InputStreamResource(stream));
    }

    @Operation(summary = "Удалить вложение")
    @DeleteMapping("/{attId}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                       @PathVariable UUID attId,
                                       @CurrentUser UserPrincipal actor) {
        attachmentService.delete(id, attId, actor);
        return ResponseEntity.noContent().build();
    }
}
