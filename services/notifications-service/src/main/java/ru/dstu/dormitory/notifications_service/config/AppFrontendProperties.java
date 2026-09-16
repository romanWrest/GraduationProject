package ru.dstu.dormitory.notifications_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.frontend")
public class AppFrontendProperties {

    private String baseUrl = "http://localhost:3000";
}
