package ru.dstu.dormitory.appliances_service.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        JwtProperties.class,
        SecurityProperties.class,
        KafkaTopicsProperties.class,
        OutboxProperties.class,
        ResidentsServiceProperties.class,
        AppProperties.class
})
public class AppPropertiesConfig {
}
