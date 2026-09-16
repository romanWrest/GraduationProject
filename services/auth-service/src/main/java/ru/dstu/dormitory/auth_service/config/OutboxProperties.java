package ru.dstu.dormitory.auth_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "outbox")
public class OutboxProperties {

    private Relay relay = new Relay();

    @Data
    public static class Relay {
        private int batchSize = 100;
        private long fixedDelayMs = 1000L;
    }
}
