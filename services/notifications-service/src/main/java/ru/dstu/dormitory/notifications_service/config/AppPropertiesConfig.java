package ru.dstu.dormitory.notifications_service.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        JwtProperties.class,
        SecurityProperties.class,
        KafkaTopicsProperties.class,
        AuthServiceProperties.class,
        AppMailProperties.class,
        AppFrontendProperties.class
})
public class AppPropertiesConfig {
}
