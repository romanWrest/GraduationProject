package ru.dstu.dormitory.requests_service.client;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import ru.dstu.dormitory.requests_service.config.SecurityProperties;

/**
 * Feign-конфиг только для residents-клиента: помимо interceptor'а с сервис-токеном
 * подменяет ErrorDecoder, чтобы 404 от residents-service трактовался как «не заселён»
 * (ResidentNotEnrolledException → 409), а не как недоступность сервиса (503).
 */
public class ResidentsFeignClientConfig {

    @Bean
    public ServiceTokenInterceptor serviceTokenInterceptor(SecurityProperties securityProperties) {
        return new ServiceTokenInterceptor(securityProperties);
    }

    @Bean
    public ErrorDecoder residentsClientErrorDecoder() {
        return new ResidentsClientErrorDecoder();
    }
}
