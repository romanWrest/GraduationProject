package ru.dstu.dormitory.requests_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "kafka.topics")
public class KafkaTopicsProperties {

    private String requestEvents = "request.events";
    private String residentEvents = "resident.events";
}
