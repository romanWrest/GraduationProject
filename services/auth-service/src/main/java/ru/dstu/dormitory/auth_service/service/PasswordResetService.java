package ru.dstu.dormitory.auth_service.service;

public interface PasswordResetService {

    /** Запросить сброс (не раскрывает существование email). */
    void requestReset(String email);

    /** Применить сброс по выданному токену. */
    void applyReset(String token, String newPassword);
}
