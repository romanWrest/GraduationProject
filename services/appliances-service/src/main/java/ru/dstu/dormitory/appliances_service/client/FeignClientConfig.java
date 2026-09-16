package ru.dstu.dormitory.appliances_service.client;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import ru.dstu.dormitory.appliances_service.config.SecurityProperties;

public class FeignClientConfig {

    @Bean
    public ServiceTokenInterceptor serviceTokenInterceptor(SecurityProperties securityProperties) {
        return new ServiceTokenInterceptor(securityProperties);
    }

    @Bean
    public ErrorDecoder residentsClientErrorDecoder() {
        return new ResidentsClientErrorDecoder();
    }
}
