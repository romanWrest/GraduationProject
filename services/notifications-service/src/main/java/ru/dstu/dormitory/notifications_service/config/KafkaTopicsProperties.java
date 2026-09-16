package ru.dstu.dormitory.notifications_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "kafka.topics")
public class KafkaTopicsProperties {

    private String userEvents = "user.events";
    private String residentEvents = "resident.events";
    private String requestEvents = "request.events";
    private String dlt = "notifications.dlt";
}
