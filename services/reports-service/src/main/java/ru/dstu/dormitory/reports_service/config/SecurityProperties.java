package ru.dstu.dormitory.reports_service.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.List;

@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    @NestedConfigurationProperty
    private Cors cors = new Cors();

    @Data
    public static class Cors {
        private List<String> allowedOrigins = List.of("http://localhost:5173");
        private List<String> allowedMethods = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
        private List<String> allowedHeaders = List.of("*");
        private List<String> exposedHeaders = List.of("Content-Disposition", "X-Request-Id");
        private boolean allowCredentials = true;
        private long maxAge = 3600L;
    }
}
