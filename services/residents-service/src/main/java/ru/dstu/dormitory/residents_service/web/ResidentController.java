package ru.dstu.dormitory.residents_service.web;

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
import ru.dstu.dormitory.residents_service.domain.enums.ResidentKind;
import ru.dstu.dormitory.residents_service.domain.model.ResidencyHistory;
import ru.dstu.dormitory.residents_service.domain.model.Resident;
import ru.dstu.dormitory.residents_service.exception.ForbiddenActionException;
import ru.dstu.dormitory.residents_service.mapper.ResidentMapper;
import ru.dstu.dormitory.residents_service.security.CurrentUser;
import ru.dstu.dormitory.residents_service.security.UserPrincipal;
import ru.dstu.dormitory.residents_service.service.ResidentService;
import ru.dstu.dormitory.residents_service.web.dto.request.EnrollResidentRequest;
import ru.dstu.dormitory.residents_service.web.dto.request.EvictResidentRequest;
import ru.dstu.dormitory.residents_service.web.dto.request.MoveResidentRequest;
import ru.dstu.dormitory.residents_service.web.dto.request.UpdateResidentRequest;
import ru.dstu.dormitory.residents_service.web.dto.response.ResidencyHistoryDto;
import ru.dstu.dormitory.residents_service.web.dto.response.ResidentDto;

import java.util.List;
import java.util.UUID;

@Tag(name = "Residents", description = "Проживающие в общежитии")
@RestController
@RequestMapping("/api/v1/residents")
@RequiredArgsConstructor
public class ResidentController {

    private final ResidentService residentService;
    private final ResidentMapper residentMapper;

    @Operation(summary = "Список проживающих с фильтрами")
    @GetMapping
    public ResponseEntity<Page<ResidentDto>> list(@RequestParam(required = false) ResidentKind kind,
                                                  @RequestParam(required = false) UUID roomId,
                                                  @RequestParam(required = false) String faculty,
                                                  @RequestParam(required = false) String search,
                                                  @RequestParam(required = false) Boolean active,
                                                  Pageable pageable) {
        Page<Resident> page = residentService.search(kind, roomId, faculty, search, active, pageable);
        return ResponseEntity.ok(page.map(residentMapper::toDto));
    }

    @Operation(summary = "Заселить жильца (ADMIN)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResidentDto> enroll(@Valid @RequestBody EnrollResidentRequest request) {
        Resident resident = residentService.enroll(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(residentMapper.toDto(resident));
    }

    @Operation(summary = "Получить жильца по id. Доступно ADMIN или владельцу")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @residentAccess.isOwner(#id, principal)")
    public ResponseEntity<ResidentDto> get(@PathVariable UUID id) {
        return ResponseEntity.ok(residentMapper.toDto(residentService.getById(id)));
    }

    @Operation(summary = "Обновить данные жильца. ADMIN — все поля; сам жилец — только phone/contactInfo")
    @PatchMapping("/{id}")
    public ResponseEntity<ResidentDto> update(@PathVariable UUID id,
                                              @Valid @RequestBody UpdateResidentRequest request,
                                              @CurrentUser UserPrincipal principal) {
        if (principal == null) {
            throw new ForbiddenActionException("Аутентификация обязательна");
        }
        boolean isAdmin = principal.roles() != null && principal.roles().stream()
                .anyMatch(r -> r.name().equals("ADMIN"));
        if (!isAdmin) {
            Resident current = residentService.getById(id);
            if (!current.getUserId().equals(principal.userId())) {
                throw new ForbiddenActionException("Нельзя менять чужой профиль жильца");
            }
        }
        Resident resident = residentService.update(id, request, !isAdmin);
        return ResponseEntity.ok(residentMapper.toDto(resident));
    }

    @Operation(summary = "Выселить жильца (ADMIN)")
    @PostMapping("/{id}/evict")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResidentDto> evict(@PathVariable UUID id,
                                             @Valid @RequestBody EvictResidentRequest request) {
        Resident resident = residentService.evict(id, request);
        return ResponseEntity.ok(residentMapper.toDto(resident));
    }

    @Operation(summary = "Переселить жильца (ADMIN)")
    @PostMapping("/{id}/move")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResidentDto> move(@PathVariable UUID id,
                                            @Valid @RequestBody MoveResidentRequest request) {
        Resident resident = residentService.move(id, request);
        return ResponseEntity.ok(residentMapper.toDto(resident));
    }

    @Operation(summary = "Найти жильца по userId. ADMIN или сам пользователь")
    @GetMapping("/by-user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == principal.userId")
    public ResponseEntity<ResidentDto> byUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(residentMapper.toDto(residentService.getByUserId(userId)));
    }

    @Operation(summary = "История проживания (смены комнат)")
    @GetMapping("/{id}/history")
    public ResponseEntity<List<ResidencyHistoryDto>> history(@PathVariable UUID id) {
        List<ResidencyHistory> history = residentService.getHistoryByResident(id);
        return ResponseEntity.ok(residentMapper.toHistoryDtoList(history));
    }
}
