package ru.dstu.dormitory.auth_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ratelimit")
public class RateLimitProperties {

    private Login login = new Login();

    @Data
    public static class Login {
        private int ipMax = 5;
        private int ipWindowSeconds = 60;
        private int emailMax = 10;
        private int emailWindowSeconds = 300;
    }
}
