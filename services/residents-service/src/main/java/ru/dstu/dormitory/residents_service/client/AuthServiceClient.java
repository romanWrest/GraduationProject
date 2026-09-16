package ru.dstu.dormitory.residents_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.dstu.dormitory.residents_service.client.dto.AuthUserDto;
import ru.dstu.dormitory.residents_service.client.dto.BatchUserIdsRequest;

import java.util.List;
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

    @PostMapping("/users/batch")
    List<AuthUserDto> getUsersBatch(@RequestBody BatchUserIdsRequest request);
}
