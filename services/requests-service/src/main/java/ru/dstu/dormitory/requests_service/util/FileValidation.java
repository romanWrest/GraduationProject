package ru.dstu.dormitory.requests_service.util;

import lombok.experimental.UtilityClass;
import ru.dstu.dormitory.requests_service.exception.AttachmentFormatNotAllowedException;
import ru.dstu.dormitory.requests_service.exception.AttachmentTooLargeException;

import java.util.Set;

@UtilityClass
public class FileValidation {

    public static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain"
    );

    public static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "pdf", "doc", "docx", "txt"
    );

    public void validateSize(long sizeBytes, long maxSizeBytes) {
        if (sizeBytes > maxSizeBytes) {
            throw new AttachmentTooLargeException(sizeBytes, maxSizeBytes);
        }
    }

    public void validateContentType(String contentType, String originalName) {
        if (contentType != null && ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            return;
        }
        String ext = extractExtension(originalName);
        if (ext == null || !ALLOWED_EXTENSIONS.contains(ext)) {
            throw new AttachmentFormatNotAllowedException(contentType == null ? "unknown" : contentType);
        }
    }

    public String sanitizeFileName(String name) {
        if (name == null) {
            return "file";
        }
        String sanitized = name.replaceAll("[^A-Za-z0-9._\\-]", "_");
        return sanitized.isBlank() ? "file" : sanitized;
    }

    private String extractExtension(String name) {
        if (name == null) {
            return null;
        }
        int idx = name.lastIndexOf('.');
        if (idx < 0 || idx == name.length() - 1) {
            return null;
        }
        return name.substring(idx + 1).toLowerCase();
    }
}
