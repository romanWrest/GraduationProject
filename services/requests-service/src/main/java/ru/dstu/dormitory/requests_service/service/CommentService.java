package ru.dstu.dormitory.requests_service.service;

import ru.dstu.dormitory.requests_service.domain.model.RequestComment;
import ru.dstu.dormitory.requests_service.security.UserPrincipal;

import java.util.List;
import java.util.UUID;

public interface CommentService {

    RequestComment addComment(UUID requestId, String body, UserPrincipal actor);

    List<RequestComment> listComments(UUID requestId, UserPrincipal actor);
}
