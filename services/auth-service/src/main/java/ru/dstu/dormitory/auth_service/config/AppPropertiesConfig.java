package ru.dstu.dormitory.auth_service.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        JwtProperties.class,
        SecurityProperties.class,
        RateLimitProperties.class,
        PasswordProperties.class,
        AdminProperties.class,
        KafkaTopicsProperties.class,
        OutboxProperties.class
})
public class AppPropertiesConfig {
}
