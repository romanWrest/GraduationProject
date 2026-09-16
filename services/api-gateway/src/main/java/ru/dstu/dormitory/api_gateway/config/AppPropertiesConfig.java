package ru.dstu.dormitory.api_gateway.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        JwtProperties.class,
        SecurityProperties.class
})
public class AppPropertiesConfig {
}
