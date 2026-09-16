package ru.dstu.dormitory.requests_service.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.dstu.dormitory.requests_service.mapper.CommentMapper;
import ru.dstu.dormitory.requests_service.security.CurrentUser;
import ru.dstu.dormitory.requests_service.security.UserPrincipal;
import ru.dstu.dormitory.requests_service.service.CommentService;
import ru.dstu.dormitory.requests_service.web.dto.CommentDto;
import ru.dstu.dormitory.requests_service.web.dto.CreateCommentDto;

import java.util.List;
import java.util.UUID;

@Tag(name = "Comments", description = "Комментарии к заявкам")
@RestController
@RequestMapping("/api/v1/requests/{id}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final CommentMapper commentMapper;

    @Operation(summary = "Добавить комментарий")
    @PostMapping
    public ResponseEntity<CommentDto> add(@PathVariable UUID id,
                                          @Valid @RequestBody CreateCommentDto dto,
                                          @CurrentUser UserPrincipal actor) {
        return ResponseEntity.ok(commentMapper.toDto(commentService.addComment(id, dto.body(), actor)));
    }

    @Operation(summary = "Список комментариев")
    @GetMapping
    public ResponseEntity<List<CommentDto>> list(@PathVariable UUID id,
                                                 @CurrentUser UserPrincipal actor) {
        return ResponseEntity.ok(commentMapper.toDtoList(commentService.listComments(id, actor)));
    }
}
