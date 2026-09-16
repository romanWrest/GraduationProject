package ru.dstu.dormitory.requests_service.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.requests_service.aspect.annotation.LogMethod;
import ru.dstu.dormitory.requests_service.domain.model.Request;
import ru.dstu.dormitory.requests_service.domain.model.RequestComment;
import ru.dstu.dormitory.requests_service.domain.repo.RequestCommentRepository;
import ru.dstu.dormitory.requests_service.exception.ForbiddenActionException;
import ru.dstu.dormitory.requests_service.security.AccessControl;
import ru.dstu.dormitory.requests_service.security.UserPrincipal;
import ru.dstu.dormitory.requests_service.service.CommentService;
import ru.dstu.dormitory.requests_service.service.RequestService;
import ru.dstu.dormitory.requests_service.util.LogPatterns;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final RequestCommentRepository commentRepository;
    private final RequestService requestService;
    private final AccessControl accessControl;

    @Override
    @LogMethod("Добавление комментария")
    @Transactional
    public RequestComment addComment(UUID requestId, String body, UserPrincipal actor) {
        Request request = requestService.getById(requestId);
        if (!accessControl.isAdmin(actor)
                && !accessControl.isAuthor(actor, request)
                && !accessControl.isAssignee(actor, request)) {
            throw new ForbiddenActionException("Комментировать могут автор, назначенный исполнитель или ADMIN");
        }
        RequestComment comment = RequestComment.builder()
                .request(request)
                .authorId(actor.userId())
                .body(body)
                .build();
        RequestComment saved = commentRepository.save(comment);
        log.info(LogPatterns.COMMENT_ADDED, requestId, actor.userId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestComment> listComments(UUID requestId, UserPrincipal actor) {
        Request request = requestService.getById(requestId);
        accessControl.requireCanView(actor, request);
        return commentRepository.findByRequestIdOrderByCreatedAtAsc(requestId);
    }
}
