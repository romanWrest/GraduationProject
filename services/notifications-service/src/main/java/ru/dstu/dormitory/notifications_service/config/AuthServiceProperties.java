package ru.dstu.dormitory.notifications_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "integration.auth")
public class AuthServiceProperties {

    private String url = "http://localhost:8081";
    private long connectTimeoutMs = 2000;
    private long readTimeoutMs = 3000;
}
