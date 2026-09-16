package ru.dstu.dormitory.reports_service.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        JwtProperties.class,
        SecurityProperties.class,
        KafkaTopicsProperties.class,
        ReportsProperties.class
})
public class AppPropertiesConfig {
}
