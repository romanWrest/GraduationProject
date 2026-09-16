package ru.dstu.dormitory.api_gateway.aspect.annotation;

/**
 * Шаблоны сообщений для {@link ru.dstu.dormitory.api_gateway.aspect.LoggingAspect}.
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
