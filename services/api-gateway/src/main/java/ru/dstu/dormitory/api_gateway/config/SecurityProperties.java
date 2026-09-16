package ru.dstu.dormitory.api_gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.List;

/**
 * Настройки CORS и общих security-параметров gateway.
 */
@Data
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    @NestedConfigurationProperty
    private Cors cors = new Cors();

    @Data
    public static class Cors {
        private List<String> allowedOrigins = List.of("http://localhost:5173");
        private List<String> allowedMethods = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
        private List<String> allowedHeaders = List.of("*");
        private List<String> exposedHeaders = List.of("X-Request-Id", "Content-Disposition", "Retry-After");
        private boolean allowCredentials = true;
        private long maxAge = 3600L;
    }
}
