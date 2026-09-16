package ru.dstu.dormitory.consumables_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.dstu.dormitory.consumables_service.client.dto.ResidentDto;

import java.util.UUID;

@FeignClient(
        name = "residents-service",
        url = "${integration.residents.url}",
        path = "/api/v1/internal",
        fallbackFactory = ResidentsServiceClientFallbackFactory.class,
        configuration = FeignClientConfig.class
)
public interface ResidentsServiceClient {

    @GetMapping("/residents/{id}")
    ResidentDto getResident(@PathVariable("id") UUID id);

    @GetMapping("/residents/by-user/{userId}")
    ResidentDto getResidentByUser(@PathVariable("userId") UUID userId);
}
