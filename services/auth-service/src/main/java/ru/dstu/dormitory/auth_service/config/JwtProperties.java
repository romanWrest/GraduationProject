package ru.dstu.dormitory.auth_service.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

import static ru.dstu.dormitory.auth_service.util.LogPatterns.JWT_SECRET_TOO_SHORT;

@Slf4j
@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;
    private Duration accessTtl = Duration.ofMinutes(15);
    private Duration refreshTtl = Duration.ofDays(7);
    private String issuer = "auth-service";

    @PostConstruct
    void validate() {
        if (secret == null || secret.length() < 32) {
            log.error(JWT_SECRET_TOO_SHORT);
            throw new IllegalStateException("JWT secret must be at least 32 characters long");
        }
    }
}
