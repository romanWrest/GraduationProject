package ru.dstu.dormitory.auth_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "admin")
public class AdminProperties {

    private String initialEmail = "admin@dormitory.local";
    private String initialPassword = "Admin12345!";
    private String initialFullName = "Администратор";
}
