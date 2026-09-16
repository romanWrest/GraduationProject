package ru.dstu.dormitory.auth_service.service;

public interface RateLimiterService {

    /**
     * Проверяет и инкрементирует счётчики логина для IP и email.
     * При превышении лимита — {@link ru.dstu.dormitory.auth_service.exception.RateLimitExceededException}.
     */
    void checkLogin(String ip, String email);
}
