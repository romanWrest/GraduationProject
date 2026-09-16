package ru.dstu.dormitory.consumables_service.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import ru.dstu.dormitory.consumables_service.config.SecurityProperties;

@RequiredArgsConstructor
public class ServiceTokenInterceptor implements RequestInterceptor {

    public static final String HEADER = "X-Service-Token";

    private final SecurityProperties securityProperties;

    @Override
    public void apply(RequestTemplate template) {
        String token = securityProperties.getServiceToken();
        if (token != null && !token.isBlank()) {
            template.header(HEADER, token);
        }
    }
}
