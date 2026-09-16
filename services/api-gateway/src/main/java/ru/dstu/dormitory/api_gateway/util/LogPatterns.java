package ru.dstu.dormitory.api_gateway.util;

/**
 * Константы строк логов для SLF4J.
 */
@SuppressWarnings("unused")
public interface LogPatterns {

    // ── Exception handler ──
    String CLIENT_ERROR = "Клиентская ошибка [{}]: {} ({})";
    String SERVER_ERROR = "Серверная ошибка [{}]: {} ({})";
    String UNHANDLED_EXCEPTION = "Необработанное исключение";

    // ── Request lifecycle ──
    String REQUEST_INCOMING = "→ {} {}, userId={}, ip={}";
    String REQUEST_ROUTED = "Маршрутизация: {} {} → {} ({}ms)";
    String REQUEST_RESPONSE = "← {} {} {} ({}ms)";

    // ── JWT ──
    String JWT_VALIDATED = "JWT валиден: userId={}, roles={}";
    String JWT_INVALID = "JWT невалиден: reason={}";
    String JWT_EXPIRED = "JWT просрочен: reason={}";
    String JWT_MISSING = "JWT отсутствует: path={}";

    // ── Rate limiting ──
    String RATE_LIMIT_HIT = "Rate limit превышен: key={}, path={}";

    // ── Downstream ──
    String DOWNSTREAM_ERROR = "Ошибка downstream: service={}, status={}, path={}";
    String DOWNSTREAM_TIMEOUT = "Timeout downstream: service={}, path={}";
    String DOWNSTREAM_UNAVAILABLE = "Downstream недоступен: service={}, path={}";

    // ── Startup ──
    String JWT_SECRET_TOO_SHORT = "JWT secret короче 32 символов — приложение не запущено";
}
