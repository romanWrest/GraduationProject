package ru.dstu.dormitory.requests_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "requests")
public class RequestsProperties {

    private int autocloseAfterDays = 3;
    private int reopenWindowDays = 7;
    private int poolStaleAfterHours = 24;
}
