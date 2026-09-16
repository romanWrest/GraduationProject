package ru.dstu.dormitory.appliances_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "kafka.topics")
public class KafkaTopicsProperties {

    private String applianceEvents = "appliance.events";
    private String residentEvents = "resident.events";
}
