package ru.dstu.dormitory.consumables_service.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.dstu.dormitory.consumables_service.client.ResidentsFacade;
import ru.dstu.dormitory.consumables_service.client.dto.ResidentDto;
import ru.dstu.dormitory.consumables_service.domain.enums.IssueStatus;
import ru.dstu.dormitory.consumables_service.domain.model.ConsumableIssue;
import ru.dstu.dormitory.consumables_service.exception.ConsumableIssueNotFoundException;
import ru.dstu.dormitory.consumables_service.mapper.ConsumableMapper;
import ru.dstu.dormitory.consumables_service.security.CurrentUser;
import ru.dstu.dormitory.consumables_service.security.UserPrincipal;
import ru.dstu.dormitory.consumables_service.service.ConsumableIssueService;
import ru.dstu.dormitory.consumables_service.web.dto.request.IssueRequest;
import ru.dstu.dormitory.consumables_service.web.dto.request.ReturnRequest;
import ru.dstu.dormitory.consumables_service.web.dto.response.ConsumableIssueDto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Tag(name = "ConsumableIssues", description = "Выдача и возврат расходников")
@RestController
@RequestMapping("/api/v1/consumables")
@RequiredArgsConstructor
public class ConsumableIssueController {

    private final ConsumableIssueService issueService;
    private final ResidentsFacade residentsFacade;
    private final ConsumableMapper mapper;

    @Operation(summary = "Создать выдачу (PROPERTY_MANAGER, ADMIN)")
    @PostMapping("/issues")
    @PreAuthorize("hasAnyRole('PROPERTY_MANAGER', 'ADMIN')")
    public ResponseEntity<ConsumableIssueDto> issue(@Valid @RequestBody IssueRequest request,
                                                    @CurrentUser UserPrincipal principal) {
        UUID actorId = principal != null ? principal.userId() : null;
        ConsumableIssue created = issueService.issue(request, actorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toIssueDto(created));
    }

    @Operation(summary = "Список выдач с фильтрами (PROPERTY_MANAGER, ADMIN)")
    @GetMapping("/issues")
    @PreAuthorize("hasAnyRole('PROPERTY_MANAGER', 'ADMIN')")
    public ResponseEntity<Page<ConsumableIssueDto>> list(@RequestParam(required = false) UUID residentId,
                                                         @RequestParam(required = false) UUID typeId,
                                                         @RequestParam(required = false) IssueStatus status,
                                                         @RequestParam(required = false) Instant from,
                                                         @RequestParam(required = false) Instant to,
                                                         Pageable pageable) {
        Page<ConsumableIssue> page = issueService.search(residentId, typeId, status, from, to, pageable);
        return ResponseEntity.ok(page.map(mapper::toIssueDto));
    }

    @Operation(summary = "Получить выдачу по id")
    @GetMapping("/issues/{id}")
    public ResponseEntity<ConsumableIssueDto> get(@PathVariable UUID id,
                                                  @CurrentUser UserPrincipal principal) {
        ConsumableIssue issue = issueService.getById(id);
        ensureCanRead(issue, principal);
        return ResponseEntity.ok(mapper.toIssueDto(issue));
    }

    @Operation(summary = "Принять возврат (PROPERTY_MANAGER, ADMIN)")
    @PatchMapping("/issues/{id}/return")
    @PreAuthorize("hasAnyRole('PROPERTY_MANAGER', 'ADMIN')")
    public ResponseEntity<ConsumableIssueDto> returnIssue(@PathVariable UUID id,
                                                          @Valid @RequestBody ReturnRequest request,
                                                          @CurrentUser UserPrincipal principal) {
        UUID actorId = principal != null ? principal.userId() : null;
        ConsumableIssue updated = issueService.returnIssue(id, request, actorId);
        return ResponseEntity.ok(mapper.toIssueDto(updated));
    }

    @Operation(summary = "Активные выдачи у жильца (PROPERTY_MANAGER, ADMIN или сам жилец)")
    @GetMapping("/by-resident/{residentId}")
    public ResponseEntity<List<ConsumableIssueDto>> byResident(@PathVariable UUID residentId,
                                                               @CurrentUser UserPrincipal principal) {
        ensureCanReadByResident(residentId, principal);
        return ResponseEntity.ok(mapper.toIssueDtoList(issueService.listIssuedByResident(residentId)));
    }

    @Operation(summary = "Мои активные выдачи (RESIDENT)")
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ConsumableIssueDto>> my(@CurrentUser UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(mapper.toIssueDtoList(issueService.listIssuedByUser(principal.userId())));
    }

    private void ensureCanRead(ConsumableIssue issue, UserPrincipal principal) {
        if (principal == null) {
            throw new ConsumableIssueNotFoundException("Выдача не найдена: id=" + issue.getId());
        }
        if (hasManagerRole(principal)) {
            return;
        }
        if (!issue.getUserId().equals(principal.userId())) {
            throw new ConsumableIssueNotFoundException("Выдача не найдена: id=" + issue.getId());
        }
    }

    private void ensureCanReadByResident(UUID residentId, UserPrincipal principal) {
        if (principal == null) {
            throw new org.springframework.security.access.AccessDeniedException("Аутентификация обязательна");
        }
        if (hasManagerRole(principal)) {
            return;
        }
        ResidentDto resident = residentsFacade.findResident(residentId);
        if (resident == null || !resident.userId().equals(principal.userId())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Просмотр чужих выдач запрещён");
        }
    }

    private boolean hasManagerRole(UserPrincipal principal) {
        return principal.roles().stream()
                .anyMatch(r -> r.name().equals("ADMIN") || r.name().equals("PROPERTY_MANAGER"));
    }
}
