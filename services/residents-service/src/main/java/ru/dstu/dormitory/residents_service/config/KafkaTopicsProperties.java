package ru.dstu.dormitory.residents_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "kafka.topics")
public class KafkaTopicsProperties {

    private String residentEvents = "resident.events";
}
