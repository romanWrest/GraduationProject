package ru.dstu.dormitory.requests_service.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.dstu.dormitory.requests_service.domain.enums.RequestStatus;
import ru.dstu.dormitory.requests_service.domain.enums.RequestType;
import ru.dstu.dormitory.requests_service.domain.model.Request;
import ru.dstu.dormitory.requests_service.mapper.AttachmentMapper;
import ru.dstu.dormitory.requests_service.mapper.CommentMapper;
import ru.dstu.dormitory.requests_service.mapper.RequestMapper;
import ru.dstu.dormitory.requests_service.mapper.StatusHistoryMapper;
import ru.dstu.dormitory.requests_service.security.CurrentUser;
import ru.dstu.dormitory.requests_service.security.UserPrincipal;
import ru.dstu.dormitory.requests_service.service.AttachmentService;
import ru.dstu.dormitory.requests_service.service.CommentService;
import ru.dstu.dormitory.requests_service.service.RequestService;
import ru.dstu.dormitory.requests_service.web.dto.CancelRequestDto;
import ru.dstu.dormitory.requests_service.web.dto.CreateRequestDto;
import ru.dstu.dormitory.requests_service.web.dto.PatchActionDto;
import ru.dstu.dormitory.requests_service.web.dto.ReopenRequestDto;
import ru.dstu.dormitory.requests_service.web.dto.RequestDetailDto;
import ru.dstu.dormitory.requests_service.web.dto.RequestDto;
import ru.dstu.dormitory.requests_service.web.dto.RequestSummaryDto;
import ru.dstu.dormitory.requests_service.web.dto.StatusHistoryDto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Tag(name = "Requests", description = "Заявки: создание, поиск, смена статуса, комментарии, вложения")
@RestController
@RequestMapping("/api/v1/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;
    private final CommentService commentService;
    private final AttachmentService attachmentService;
    private final RequestMapper requestMapper;
    private final CommentMapper commentMapper;
    private final AttachmentMapper attachmentMapper;
    private final StatusHistoryMapper historyMapper;
    private final ObjectMapper objectMapper;

    @Operation(summary = "Создать заявку (multipart: request + files)")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RequestDto> create(@RequestPart("request") String requestJson,
                                             @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                             @CurrentUser UserPrincipal actor) throws JsonProcessingException {
        CreateRequestDto dto = objectMapper.readValue(requestJson, CreateRequestDto.class);
        Request created = requestService.create(dto, actor, files);
        return ResponseEntity.ok(requestMapper.toDto(created));
    }

    @Operation(summary = "Список заявок с фильтрами и пагинацией")
    @GetMapping
    public ResponseEntity<Page<RequestSummaryDto>> list(@RequestParam(required = false) RequestType type,
                                                        @RequestParam(required = false) RequestStatus status,
                                                        @RequestParam(required = false) UUID assigneeId,
                                                        @RequestParam(required = false) UUID authorId,
                                                        @RequestParam(required = false) UUID roomId,
                                                        @RequestParam(required = false) Instant from,
                                                        @RequestParam(required = false) Instant to,
                                                        @RequestParam(required = false) String search,
                                                        @CurrentUser UserPrincipal actor,
                                                        @PageableDefault(size = 20, sort = "createdAt",
                                                                direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Request> page = requestService.search(actor, type, status, assigneeId, authorId, roomId,
                from, to, search, pageable);
        return ResponseEntity.ok(page.map(requestMapper::toSummaryDto));
    }

    @Operation(summary = "Мои заявки (shortcut к /requests?authorId=self)")
    @GetMapping("/my")
    public ResponseEntity<Page<RequestSummaryDto>> my(@CurrentUser UserPrincipal actor,
                                                      @PageableDefault(size = 20, sort = "createdAt",
                                                              direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Request> page = requestService.search(actor, null, null, null, actor.userId(), null,
                null, null, null, pageable);
        return ResponseEntity.ok(page.map(requestMapper::toSummaryDto));
    }

    @Operation(summary = "Заявки в пуле (по роли пользователя)")
    @GetMapping("/pool")
    public ResponseEntity<Page<RequestSummaryDto>> pool(@CurrentUser UserPrincipal actor,
                                                        @PageableDefault(size = 20, sort = "createdAt",
                                                                direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Request> page = requestService.pool(actor, pageable);
        return ResponseEntity.ok(page.map(requestMapper::toSummaryDto));
    }

    @Operation(summary = "Детали заявки (с комментариями и вложениями)")
    @GetMapping("/{id}")
    public ResponseEntity<RequestDetailDto> get(@PathVariable UUID id,
                                                @CurrentUser UserPrincipal actor) {
        Request request = requestService.getById(id);
        // требование прав просмотра обрабатывается в сервисе при загрузке комментов/вложений
        RequestDetailDto dto = new RequestDetailDto(
                requestMapper.toDto(request),
                commentMapper.toDtoList(commentService.listComments(id, actor)),
                attachmentMapper.toDtoList(attachmentService.list(id, actor))
        );
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Действие над заявкой: ASSIGN/START/DONE/CONFIRM/REJECT/REVIEW")
    @PatchMapping("/{id}")
    public ResponseEntity<RequestDto> patch(@PathVariable UUID id,
                                            @Valid @RequestBody PatchActionDto dto,
                                            @CurrentUser UserPrincipal actor) {
        Request result = switch (dto.action()) {
            case ASSIGN -> requestService.assign(id, dto.assigneeId(), dto.scheduledAt(), actor);
            case START -> requestService.start(id, actor);
            case DONE -> requestService.done(id, dto.resolutionComment(), actor);
            case CONFIRM -> requestService.confirm(id, actor);
            case REJECT -> requestService.reject(id, dto.rejectionReason(), actor);
            case REVIEW -> requestService.review(id, actor);
        };
        return ResponseEntity.ok(requestMapper.toDto(result));
    }

    @Operation(summary = "Отозвать заявку (только автор)")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<RequestDto> cancel(@PathVariable UUID id,
                                             @Valid @RequestBody(required = false) CancelRequestDto dto,
                                             @CurrentUser UserPrincipal actor) {
        String reason = dto == null ? null : dto.reason();
        Request result = requestService.cancel(id, reason, actor);
        return ResponseEntity.ok(requestMapper.toDto(result));
    }

    @Operation(summary = "Переоткрыть заявку (только автор, окно 7 дней)")
    @PostMapping("/{id}/reopen")
    public ResponseEntity<RequestDto> reopen(@PathVariable UUID id,
                                             @Valid @RequestBody ReopenRequestDto dto,
                                             @CurrentUser UserPrincipal actor) {
        Request result = requestService.reopen(id, dto.reason(), actor);
        return ResponseEntity.ok(requestMapper.toDto(result));
    }

    @Operation(summary = "История смены статусов")
    @GetMapping("/{id}/history")
    public ResponseEntity<List<StatusHistoryDto>> history(@PathVariable UUID id,
                                                          @CurrentUser UserPrincipal actor) {
        return ResponseEntity.ok(historyMapper.toDtoList(requestService.history(id, actor)));
    }
}
