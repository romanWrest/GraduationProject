package ru.dstu.dormitory.reports_service.web;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.dstu.dormitory.reports_service.service.export.ExportFormat;
import ru.dstu.dormitory.reports_service.service.export.ExportService;
import ru.dstu.dormitory.reports_service.service.export.ExportService.ExportParams;
import ru.dstu.dormitory.reports_service.service.export.ExportService.ExportResult;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    @GetMapping("/{name}/export")
    public ResponseEntity<byte[]> export(@PathVariable String name,
                                         @RequestParam String format,
                                         @RequestParam(required = false) Instant from,
                                         @RequestParam(required = false) Instant to,
                                         @RequestParam(required = false)
                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOf,
                                         @RequestParam(required = false) UUID typeId,
                                         @RequestParam(required = false) UUID assigneeId,
                                         @RequestParam(required = false) UUID roomId) {
        ExportFormat fmt = ExportFormat.from(format);
        ExportParams params = new ExportParams(from, to, asOf, typeId, assigneeId, roomId);
        ExportResult result = exportService.export(name, fmt, params);

        String fileName = "report-%s%s.%s".formatted(
                name,
                periodSuffix(from, to, asOf),
                fmt.extension());

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"%s\"".formatted(fileName));
        headers.setContentType(MediaType.parseMediaType(fmt.contentType()));
        headers.setContentLength(result.bytes().length);

        return ResponseEntity.ok().headers(headers).body(result.bytes());
    }

    private String periodSuffix(Instant from, Instant to, LocalDate asOf) {
        if (asOf != null) {
            return "-" + asOf;
        }
        if (from != null && to != null) {
            return "-%s-%s".formatted(from.toString().substring(0, 10), to.toString().substring(0, 10));
        }
        return "";
    }
}
