package ru.dstu.dormitory.auth_service.service.event;

public interface EventTypes {

    String USER_CREATED = "UserCreated";
    String USER_DEACTIVATED = "UserDeactivated";
    String PASSWORD_RESET_REQUESTED = "PasswordResetRequested";

    String AGGREGATE_USER = "User";
}
