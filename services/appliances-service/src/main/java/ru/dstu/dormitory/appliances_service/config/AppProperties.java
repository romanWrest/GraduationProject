package ru.dstu.dormitory.appliances_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /**
     * Лимит суммарной мощности приборов на одну комнату в ваттах.
     */
    private int roomPowerLimitWatts = 3500;
}
