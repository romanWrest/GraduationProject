package ru.dstu.dormitory.reports_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "kafka.topics")
public class KafkaTopicsProperties {

    private String userEvents = "user.events";
    private String residentEvents = "resident.events";
    private String requestEvents = "request.events";
    private String applianceEvents = "appliance.events";
    private String consumableEvents = "consumable.events";
    private String dlt = "reports.dlt";
}
