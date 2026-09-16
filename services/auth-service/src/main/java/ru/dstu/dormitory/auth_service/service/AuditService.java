package ru.dstu.dormitory.auth_service.service;

import java.util.Map;
import java.util.UUID;

public interface AuditService {

    interface Action {
        String LOGIN_SUCCESS = "LOGIN_SUCCESS";
        String LOGIN_FAILED = "LOGIN_FAILED";
        String USER_CREATE = "USER_CREATE";
        String USER_UPDATE = "USER_UPDATE";
        String USER_DEACTIVATE = "USER_DEACTIVATE";
        String ROLES_CHANGE = "ROLES_CHANGE";
        String PASSWORD_RESET_REQUEST = "PASSWORD_RESET_REQUEST";
        String PASSWORD_CHANGE = "PASSWORD_CHANGE";
    }

    void record(UUID actorId, String action, String targetType, String targetId, Map<String, Object> meta);
}
