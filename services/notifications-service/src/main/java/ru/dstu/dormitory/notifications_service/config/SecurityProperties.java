package ru.dstu.dormitory.notifications_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    private Cors cors = new Cors();
    private String serviceToken;

    @Data
    public static class Cors {
        private List<String> allowedOrigins = List.of("http://localhost:3000");
        private List<String> allowedMethods = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
        private List<String> allowedHeaders = List.of("*");
        private List<String> exposedHeaders = List.of("Authorization", "Retry-After");
        private boolean allowCredentials = true;
        private long maxAge = 3600;
    }
}
