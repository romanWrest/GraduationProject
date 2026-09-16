package ru.dstu.dormitory.requests_service.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.dstu.dormitory.requests_service.exception.StorageUnavailableException;
import ru.dstu.dormitory.requests_service.util.LogPatterns;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    private final MinioProperties properties;

    @Bean
    public MinioClient minioClient() {
        MinioClient client = MinioClient.builder()
                .endpoint(properties.getUrl())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
        ensureBucket(client, properties.getBucket());
        return client;
    }

    private void ensureBucket(MinioClient client, String bucket) {
        try {
            if (!client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
            log.info(LogPatterns.MINIO_BUCKET_READY, bucket);
        } catch (Exception ex) {
            log.error(LogPatterns.MINIO_ERROR, "ensureBucket", ex.getMessage());
            throw new StorageUnavailableException(
                    "Не удалось инициализировать MinIO bucket: " + bucket, ex);
        }
    }
}