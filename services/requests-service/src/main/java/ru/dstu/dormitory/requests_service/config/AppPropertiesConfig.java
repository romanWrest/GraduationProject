package ru.dstu.dormitory.requests_service.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        JwtProperties.class,
        SecurityProperties.class,
        KafkaTopicsProperties.class,
        OutboxProperties.class,
        ResidentsServiceProperties.class,
        AuthServiceProperties.class,
        MinioProperties.class,
        RequestsProperties.class
})
public class AppPropertiesConfig {
}
