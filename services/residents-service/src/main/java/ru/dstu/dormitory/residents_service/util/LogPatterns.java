package ru.dstu.dormitory.residents_service.util;

/**
 * Константы строк логов для SLF4J.
 * Все шаблоны используют {}-плейсхолдеры SLF4J либо %s для String.format.
 */
@SuppressWarnings("unused")
public interface LogPatterns {

    // ── Validation patterns (regex) ──
    String UUID_PATTERN = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
    String UUID_MESSAGE_ERROR_PATTERN = "Ошибка формата UUID";

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

    // ── Жильцы ──
    String RESIDENT_ENROLLED = "Жилец заселён: id={}, userId={}, roomId={}, kind={}";
    String RESIDENT_EVICTED = "Жилец выселен: id={}, evictedAt={}";
    String RESIDENT_MOVED = "Жилец переселён: id={}, fromRoom={}, toRoom={}";
    String RESIDENT_UPDATED = "Данные жильца обновлены: id={}";

    // ── Комнаты ──
    String ROOM_CREATED = "Комната создана: id={}, number={}, floor={}";
    String ROOM_UPDATED = "Комната обновлена: id={}";
    String ROOM_DELETED = "Комната удалена: id={}";

    // ── Инвентарь ──
    String INVENTORY_ADDED = "Позиция инвентаря добавлена: id={}, roomId={}, type={}";
    String INVENTORY_STATE_CHANGED = "Состояние инвентаря изменено: id={}, oldState={}, newState={}";
    String INVENTORY_WRITTEN_OFF = "Инвентарь списан: id={}, reason={}";

    // ── Outbox / Kafka ──
    String OUTBOX_SAVED = "Событие сохранено в outbox: eventId={}, type={}";
    String OUTBOX_PUBLISHED = "Событие отправлено в Kafka: eventId={}, type={}";
    String OUTBOX_PUBLISH_FAILED = "Не удалось отправить событие: eventId={}, cause={}";

    // ── Интеграция с auth ──
    String AUTH_USER_SYNCED = "Синхронизирован пользователь из auth: userId={}";
    String AUTH_SERVICE_UNAVAILABLE = "auth-service недоступен: {}";
    String AUTH_USER_NOT_FOUND = "Пользователь отсутствует в auth: userId={}";

    // ── JWT ──
    String JWT_INVALID = "Невалидный JWT: {}";
    String JWT_EXPIRED = "JWT просрочен: {}";

    // ── Startup ──
    String JWT_SECRET_TOO_SHORT = "JWT secret короче 32 символов — приложение не запущено";
}
