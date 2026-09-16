package ru.dstu.dormitory.residents_service.client;

import org.springframework.context.annotation.Bean;
import ru.dstu.dormitory.residents_service.config.SecurityProperties;

public class FeignClientConfig {

    @Bean
    public ServiceTokenInterceptor serviceTokenInterceptor(SecurityProperties securityProperties) {
        return new ServiceTokenInterceptor(securityProperties);
    }
}
