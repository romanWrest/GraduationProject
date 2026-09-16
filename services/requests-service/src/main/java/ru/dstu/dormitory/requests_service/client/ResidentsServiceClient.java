package ru.dstu.dormitory.requests_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.dstu.dormitory.requests_service.client.dto.ResidentDto;
import ru.dstu.dormitory.requests_service.client.dto.RoomDto;

import java.util.UUID;

@FeignClient(
        name = "residents-service",
        url = "${integration.residents.url}",
        path = "/api/v1/internal",
        fallbackFactory = ResidentsServiceClientFallbackFactory.class,
        configuration = ResidentsFeignClientConfig.class
)
public interface ResidentsServiceClient {

    @GetMapping("/residents/by-user/{userId}")
    ResidentDto getResidentByUser(@PathVariable("userId") UUID userId);

    @GetMapping("/rooms/{id}")
    RoomDto getRoom(@PathVariable("id") UUID id);
}
