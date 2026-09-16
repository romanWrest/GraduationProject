package ru.dstu.dormitory.requests_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.dstu.dormitory.requests_service.client.dto.AuthUserDto;

import java.util.UUID;

@FeignClient(
        name = "auth-service",
        url = "${integration.auth.url}",
        path = "/api/v1/internal",
        fallback = AuthServiceClientFallback.class,
        configuration = FeignClientConfig.class
)
public interface AuthServiceClient {

    @GetMapping("/users/{id}")
    AuthUserDto getUser(@PathVariable("id") UUID id);
}
