package ru.dstu.dormitory.notifications_service.aspect.annotation;

/**
 * Шаблоны сообщений для {@link ru.dstu.dormitory.notifications_service.aspect.LoggingAspect}.
 */
public interface LogMessages {

    String METHOD_START_NAME = "→ {}";
    String METHOD_START_NAME_MESSAGE = "→ {} — {}";
    String METHOD_START_NAME_ARGUMENTS = "→ {} аргументы: {}";
    String METHOD_START_NAME_MESSAGE_ARGUMENTS = "→ {} — {} аргументы: {}";

    String METHOD_END = "✓ {} за {} мс";
    String METHOD_END_WITH_RESULT = "✓ {} за {} мс, результат: {}";

    String METHOD_ERROR = "✗ {}: {}";
}
