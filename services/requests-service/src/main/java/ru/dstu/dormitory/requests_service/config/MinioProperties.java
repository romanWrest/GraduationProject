package ru.dstu.dormitory.requests_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    private String url = "http://localhost:9000";
    private String accessKey = "minioadmin";
    private String secretKey = "minioadmin";
    private String bucket = "requests-attachments";
    private long maxFileSizeBytes = 10L * 1024 * 1024;
}
