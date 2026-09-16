package ru.dstu.dormitory.consumables_service.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        JwtProperties.class,
        SecurityProperties.class,
        KafkaTopicsProperties.class,
        OutboxProperties.class,
        ResidentsServiceProperties.class
})
public class AppPropertiesConfig {
}
