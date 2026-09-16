package ru.dstu.dormitory.consumables_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "kafka.topics")
public class KafkaTopicsProperties {

    private String consumableEvents = "consumable.events";
    private String residentEvents = "resident.events";
    private String notificationEvents = "notification.events";
}
