package ru.dstu.dormitory.consumables_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "integration.residents")
public class ResidentsServiceProperties {

    private String url = "http://localhost:8083";
    private long connectTimeoutMs = 2000;
    private long readTimeoutMs = 3000;
}
