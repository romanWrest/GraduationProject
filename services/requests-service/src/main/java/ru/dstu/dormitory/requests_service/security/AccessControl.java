package ru.dstu.dormitory.requests_service.security;

import org.springframework.stereotype.Component;
import ru.dstu.dormitory.requests_service.domain.enums.RoleCode;
import ru.dstu.dormitory.requests_service.domain.enums.TargetPool;
import ru.dstu.dormitory.requests_service.domain.model.Request;
import ru.dstu.dormitory.requests_service.exception.ForbiddenActionException;

import java.util.UUID;

@Component
public class AccessControl {

    public boolean isAdmin(UserPrincipal user) {
        return user != null && user.roles().contains(RoleCode.ADMIN);
    }

    public boolean isAuthor(UserPrincipal user, Request request) {
        return user != null && request.getAuthorId() != null && request.getAuthorId().equals(user.userId());
    }

    public boolean isAssignee(UserPrincipal user, Request request) {
        return user != null && request.getAssigneeId() != null && request.getAssigneeId().equals(user.userId());
    }

    public boolean isInTargetPool(UserPrincipal user, Request request) {
        if (user == null || request.getTargetPool() == null) {
            return false;
        }
        return matchesPool(user, request.getTargetPool());
    }

    public boolean matchesPool(UserPrincipal user, TargetPool pool) {
        if (user == null || pool == null) {
            return false;
        }
        if (pool == TargetPool.ADMIN) {
            return isAdmin(user);
        }
        try {
            RoleCode required = RoleCode.valueOf(pool.name());
            return user.roles().contains(required);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public boolean canViewRequest(UserPrincipal user, Request request) {
        return isAdmin(user)
                || isAuthor(user, request)
                || isAssignee(user, request)
                || isInTargetPool(user, request);
    }

    public void requireCanView(UserPrincipal user, Request request) {
        if (!canViewRequest(user, request)) {
            throw new ForbiddenActionException("Нет прав на просмотр заявки");
        }
    }

    public UUID requireUserId(UserPrincipal user) {
        if (user == null || user.userId() == null) {
            throw new ForbiddenActionException("Требуется аутентификация");
        }
        return user.userId();
    }
}
