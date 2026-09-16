package ru.dstu.dormitory.notifications_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.mail")
public class AppMailProperties {

    private String fromAddress = "noreply@dormitory.local";
    private String fromName = "Dormitory System";
}
