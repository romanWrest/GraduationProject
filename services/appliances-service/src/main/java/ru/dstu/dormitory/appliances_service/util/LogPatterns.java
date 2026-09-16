package ru.dstu.dormitory.appliances_service.util;

/**
 * Константы строк логов для SLF4J.
 * Все шаблоны используют {}-плейсхолдеры SLF4J либо %s для String.format.
 */
@SuppressWarnings("unused")
public interface LogPatterns {

    // ── Validation patterns (regex) ──
    String UUID_PATTERN = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

    // ── Валидация ──
    String VALIDATION_ERROR = "Ошибка валидации поле [{}, {}]: {}";
    String CACHE_CHECK_ERROR = "Ошибка при проверке кэша: {}";

    // ── Exception handler (SLF4J) ──
    String CLIENT_ERROR = "Клиентская ошибка [{}]: {} ({})";
    String SERVER_ERROR = "Серверная ошибка [{}]: {} ({})";
    String UNHANDLED_EXCEPTION = "Необработанное исключение";
    String INVALID_JSON_FORMAT = "Некорректный формат JSON";

    // ── Exception descriptions (String.format) ──
    String PATH_NOT_FOUND = "Путь не найден: /%s";
    String MISSING_PARAM = "Отсутствует обязательный параметр запроса: '%s' (тип: %s)";
    String MISSING_HEADER = "Отсутствует обязательный заголовок: '%s'";
    String MISSING_PATH_VARIABLE = "Отсутствует переменная пути: '%s'";
    String TYPE_MISMATCH = "Параметр '%s' имеет неверный тип. Ожидается: %s";
    String METHOD_NOT_SUPPORTED = "Метод '%s' не поддерживается. Доступные методы: %s";
    String SUPPORTED_METHODS_UNDEFINED = "не определены";
    String RESOURCE_NOT_FOUND = "Ресурс не найден";

    // ── Бизнес-операции ──
    String APPLIANCE_REGISTERED = "Прибор зарегистрирован: id={}, residentId={}, type={}, power={}";
    String APPLIANCE_APPROVED = "Прибор одобрен: id={}, by={}";
    String APPLIANCE_REJECTED = "Прибор отклонён: id={}, by={}, reason={}";
    String APPLIANCE_REVOKED = "Прибор снят с учёта: id={}, by={}, reason={}";
    String APPLIANCE_AUTO_REVOKED = "Прибор автоматически снят (выселение): id={}, residentId={}";

    String ROOM_POWER_CALCULATED = "Расчёт мощности комнаты: roomId={}, totalWatts={}, applianceCount={}";

    // ── События ──
    String EVENT_RECEIVED = "Получено событие: type={}, eventId={}";
    String EVENT_PROCESSED = "Событие обработано: type={}, eventId={}";
    String EVENT_DUPLICATE = "Дубликат события: eventId={}";

    // ── Outbox / Kafka ──
    String OUTBOX_SAVED = "Событие сохранено в outbox: eventId={}, type={}";
    String OUTBOX_PUBLISHED = "Событие отправлено в Kafka: eventId={}, type={}";
    String OUTBOX_PUBLISH_FAILED = "Не удалось отправить событие: eventId={}, cause={}";

    // ── Интеграция с residents ──
    String RESIDENTS_CLIENT_CALL = "Запрос в residents: path={}, result={}";
    String RESIDENTS_CLIENT_FALLBACK = "Сработал fallback residents: reason={}";

    // ── JWT ──
    String JWT_INVALID = "Невалидный JWT: {}";
    String JWT_EXPIRED = "JWT просрочен: {}";

    // ── Startup ──
    String JWT_SECRET_TOO_SHORT = "JWT secret короче 32 символов — приложение не запущено";
}
