package ru.dstu.dormitory.consumables_service.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.dstu.dormitory.consumables_service.mapper.ConsumableMapper;
import ru.dstu.dormitory.consumables_service.service.ConsumableIssueService;
import ru.dstu.dormitory.consumables_service.web.dto.response.ConsumableIssueDto;

import java.util.List;
import java.util.UUID;

@Tag(name = "Internal", description = "Служебные эндпоинты для межсервисного взаимодействия")
@RestController
@RequestMapping("/api/v1/internal/consumables")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SERVICE', 'ADMIN')")
public class InternalController {

    private final ConsumableIssueService issueService;
    private final ConsumableMapper mapper;

    @Operation(summary = "Активные выдачи у жильца (service-to-service)")
    @GetMapping("/by-resident/{residentId}")
    public ResponseEntity<List<ConsumableIssueDto>> byResident(@PathVariable UUID residentId) {
        return ResponseEntity.ok(mapper.toIssueDtoList(issueService.listIssuedByResident(residentId)));
    }
}
