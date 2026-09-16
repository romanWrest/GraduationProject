package ru.dstu.dormitory.appliances_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "outbox.relay")
public class OutboxProperties {

    private int batchSize = 100;
    private long fixedDelayMs = 1000;
}
