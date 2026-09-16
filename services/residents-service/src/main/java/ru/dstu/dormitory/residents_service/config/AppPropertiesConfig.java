package ru.dstu.dormitory.residents_service.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        JwtProperties.class,
        SecurityProperties.class,
        KafkaTopicsProperties.class,
        OutboxProperties.class,
        AuthServiceProperties.class
})
public class AppPropertiesConfig {
}
