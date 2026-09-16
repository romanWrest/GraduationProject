package ru.dstu.dormitory.requests_service.service.storage;

import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import io.minio.MinioClient;
import ru.dstu.dormitory.requests_service.config.MinioProperties;
import ru.dstu.dormitory.requests_service.exception.StorageUnavailableException;
import ru.dstu.dormitory.requests_service.util.LogPatterns;

import java.io.InputStream;
import java.util.UUID;

/**
 * Инкапсулирует работу с MinIO: загрузка, выгрузка, удаление.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;
    private final MinioProperties properties;

    /**
     * Сформировать ключ объекта: {requestId}/{attachmentId}/{sanitized-name}.
     */
    public String buildObjectKey(UUID requestId, UUID attachmentId, String sanitizedName) {
        return "%s/%s/%s".formatted(requestId, attachmentId, sanitizedName);
    }

    public String getBucket() {
        return properties.getBucket();
    }

    /**
     * Сохранить объект в MinIO.
     */
    public void upload(String objectKey, InputStream inputStream, long size, String contentType) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectKey)
                    .stream(inputStream, size, -1)
                    .contentType(contentType == null ? "application/octet-stream" : contentType)
                    .build());
            log.info(LogPatterns.MINIO_UPLOAD, properties.getBucket(), objectKey, size);
        } catch (Exception ex) {
            log.error(LogPatterns.MINIO_ERROR, "upload", ex.getMessage());
            throw new StorageUnavailableException("Не удалось загрузить объект в MinIO: " + objectKey, ex);
        }
    }

    /**
     * Получить объект из MinIO для скачивания клиентом.
     */
    public GetObjectResponse download(String objectKey) {
        try {
            log.info(LogPatterns.MINIO_DOWNLOAD, properties.getBucket(), objectKey);
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectKey)
                    .build());
        } catch (Exception ex) {
            log.error(LogPatterns.MINIO_ERROR, "download", ex.getMessage());
            throw new StorageUnavailableException("Не удалось получить объект из MinIO: " + objectKey, ex);
        }
    }

    /**
     * Удалить объект (best-effort для compensation).
     */
    public void delete(String objectKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectKey)
                    .build());
        } catch (Exception ex) {
            log.warn(LogPatterns.MINIO_ERROR, "delete(%s)".formatted(objectKey), ex.getMessage());
        }
    }
}
