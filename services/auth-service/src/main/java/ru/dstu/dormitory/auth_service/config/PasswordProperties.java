package ru.dstu.dormitory.auth_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "password")
public class PasswordProperties {

    private int bcryptCost = 12;
    private int resetTtlMinutes = 30;
}
