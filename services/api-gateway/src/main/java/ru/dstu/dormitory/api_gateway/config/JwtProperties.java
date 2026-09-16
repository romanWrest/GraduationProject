package ru.dstu.dormitory.api_gateway.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

import static ru.dstu.dormitory.api_gateway.util.LogPatterns.JWT_SECRET_TOO_SHORT;

/**
 * Свойства подписи и валидации JWT.
 */
@Slf4j
@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;
    private String issuer = "auth-service";

    /**
     * Пути, для которых JWT-валидация пропускается (Ant-стиль).
     */
    private List<String> publicPaths = new ArrayList<>();

    @PostConstruct
    void validate() {
        if (secret == null || secret.length() < 32) {
            log.error(JWT_SECRET_TOO_SHORT);
            throw new IllegalStateException("JWT secret must be at least 32 characters long");
        }
    }
}
