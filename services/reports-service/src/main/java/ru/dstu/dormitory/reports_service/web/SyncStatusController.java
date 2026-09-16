package ru.dstu.dormitory.reports_service.web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.dstu.dormitory.reports_service.service.SyncStatusService;
import ru.dstu.dormitory.reports_service.web.dto.response.SyncStatusDto;

@RestController
@RequestMapping("/api/v1/reports/sync-status")
@RequiredArgsConstructor
public class SyncStatusController {

    private final SyncStatusService syncStatusService;

    @GetMapping
    public SyncStatusDto status() {
        return syncStatusService.status();
    }
}
