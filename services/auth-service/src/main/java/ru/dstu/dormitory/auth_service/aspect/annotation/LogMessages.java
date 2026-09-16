package ru.dstu.dormitory.auth_service.aspect.annotation;

/**
 * Шаблоны сообщений для {@link ru.dstu.dormitory.auth_service.aspect.LoggingAspect}.
 * Используют SLF4J-плейсхолдеры {}.
 */
public interface LogMessages {

    // ── Вход в метод ──
    String METHOD_START_NAME = "→ {}";
    String METHOD_START_NAME_MESSAGE = "→ {} — {}";
    String METHOD_START_NAME_ARGUMENTS = "→ {} аргументы: {}";
    String METHOD_START_NAME_MESSAGE_ARGUMENTS = "→ {} — {} аргументы: {}";

    // ── Выход из метода ──
    String METHOD_END = "✓ {} за {} мс";
    String METHOD_END_WITH_RESULT = "✓ {} за {} мс, результат: {}";

    // ── Ошибка ──
    String METHOD_ERROR = "✗ {}: {}";
}