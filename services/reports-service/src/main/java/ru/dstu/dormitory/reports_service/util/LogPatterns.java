package ru.dstu.dormitory.reports_service.util;

@SuppressWarnings("unused")
public interface LogPatterns {

    // ── Exception handler ──
    String CLIENT_ERROR = "Клиентская ошибка [{}]: {} ({})";
    String SERVER_ERROR = "Серверная ошибка [{}]: {} ({})";
    String UNHANDLED_EXCEPTION = "Необработанное исключение";
    String VALIDATION_ERROR = "Ошибка валидации поле [{}, {}]: {}";

    // ── Описания исключений (String.format) ──
    String PATH_NOT_FOUND = "Путь не найден: /%s";
    String MISSING_PARAM = "Отсутствует обязательный параметр запроса: '%s' (тип: %s)";
    String MISSING_HEADER = "Отсутствует обязательный заголовок: '%s'";
    String MISSING_PATH_VARIABLE = "Отсутствует переменная пути: '%s'";
    String TYPE_MISMATCH = "Параметр '%s' имеет неверный тип. Ожидается: %s";
    String METHOD_NOT_SUPPORTED = "Метод '%s' не поддерживается. Доступные методы: %s";
    String SUPPORTED_METHODS_UNDEFINED = "не определены";
    String RESOURCE_NOT_FOUND = "Ресурс не найден";
    String INVALID_JSON_FORMAT = "Некорректный формат JSON";

    // ── Kafka events ──
    String EVENT_RECEIVED = "Получено событие: topic={}, type={}, eventId={}";
    String EVENT_PROCESSED = "Событие обработано: type={}, eventId={}, ms={}";
    String EVENT_DUPLICATE = "Дубликат события: eventId={}";
    String EVENT_FAILED = "Ошибка обработки события: type={}, eventId={}, cause={}";
    String EVENT_SKIPPED = "Событие пропущено: type={}, eventId={}, reason={}";

    // ── Read-модели ──
    String READ_MODEL_UPDATED = "Read-модель обновлена: model={}, aggregateId={}";
    String LATE_JOIN_APPLIED = "Поздний JOIN применён: target={}, count={}";

    // ── Reports ──
    String REPORT_BUILT = "Отчёт построен: name={}, period={}, rowCount={}, ms={}";
    String REPORT_EXPORTED = "Отчёт экспортирован: name={}, format={}, sizeBytes={}, ms={}";
    String EXPORT_FAILED = "Ошибка экспорта: name={}, format={}, cause={}";

    // ── JWT ──
    String JWT_INVALID = "Невалидный JWT: {}";
    String JWT_EXPIRED = "JWT просрочен: {}";

    // ── Startup ──
    String JWT_SECRET_TOO_SHORT = "JWT secret короче 32 символов — приложение не запущено";
}
