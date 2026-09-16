package ru.dstu.dormitory.notifications_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.dstu.dormitory.notifications_service.client.dto.AuthUserDto;

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

    /*
     * DECISION: эндпоинт получения списка по роли указан в ТЗ как
     * GET /api/v1/internal/users?role=ADMIN. В auth-service такой метод
     * не реализован — fallback вернёт пустой список и notifications-service
     * не будет рассылать уведомления администраторам, пока эндпоинт не появится.
     */
    @GetMapping("/users")
    List<AuthUserDto> getUsersByRole(@RequestParam("role") String role);
}
