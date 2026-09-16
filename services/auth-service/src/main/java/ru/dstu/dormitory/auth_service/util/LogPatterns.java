package ru.dstu.dormitory.auth_service.util;

/**
 * Константы строк логов для SLF4J.
 * Все шаблоны используют {}-плейсхолдеры SLF4J либо %s для String.format.
 */
@SuppressWarnings("unused")
public interface LogPatterns {

    // ── Validation patterns (regex) ──
    String UUID_PATTERN = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
    String RUSSIAN_ALPHABET_PATTERN = "^[А-Яа-яЁё]+$";
    String PASSWORD_PATTERN = "^(?=.*[A-Za-zА-Яа-яЁё])(?=.*\\d).{8,}$";
    String UUID_MESSAGE_ERROR_PATTERN = "Ошибка формата UUID";

    // ── Kafka / DLT ──
    String NULL_KEY = "[DLT] Нулевой ключ - offset: {}, value: {}, timestamp: {}";
    String KEY_VALUE = "[DLT] KEY [{}], VALUE [{}]";
    String ERROR_TO_SEND_KAFKA = "Ошибка отправки в Kafka, key=[{}]: {}";
    String SUCCESSFULLY_TO_SEND_KAFKA = "Успешно отправлено в Kafka, key=[{}]: {}";
    String NULL_MESSAGE_OFFSET = "Получено null сообщение, оффсет=[{}]";
    String NULL_KEY_OFFSET = "Получен null ключ, оффсет=[{}]";
    String INVALID_KEY_OFFSET = "Невалидный ключ: [{}], оффсет: [{}]";

    // ── Валидация и кэш ──
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
    String REACHED_REGISTRATION_LIMIT = "Исчерпан лимит регистрации";
    String RESOURCE_NOT_FOUND = "Ресурс не найден";
    String CONSTRAINT_VIOLATION_FIELD = "Поле '%s': %s";

    // ── Пользователи ──
    String USER_CREATED = "Пользователь создан: id={}, email={}";
    String USER_DEACTIVATED = "Пользователь деактивирован: id={}";
    String USER_ACTIVATED = "Пользователь активирован: id={}";
    String USER_UPDATED = "Пользователь обновлён: id={}";
    String USER_ROLES_CHANGED = "Роли пользователя изменены: id={}, roles={}";
    String USER_NOT_FOUND = "Пользователь не найден: id={}";

    // ── Аутентификация ──
    String LOGIN_SUCCESS = "Успешный логин: email={}";
    String LOGIN_FAILED = "Неуспешный логин: email={}";
    String LOGOUT_SUCCESS = "Выход выполнен: userId={}";
    String REFRESH_ISSUED = "Refresh выдан: userId={}, familyId={}";
    String REFRESH_REVOKED = "Refresh отозван: tokenHash={}";
    String REFRESH_REUSE_DETECTED = "Обнаружено переиспользование refresh: userId={}, familyId={}, семья инвалидирована";

    // ── Пароли ──
    String PASSWORD_RESET_REQUESTED = "Запрошен сброс пароля: email={}";
    String PASSWORD_RESET_COMPLETED = "Пароль сброшен: userId={}";
    String PASSWORD_CHANGED = "Пароль изменён: userId={}";

    // ── Outbox / Kafka ──
    String OUTBOX_SAVED = "Событие сохранено в outbox: eventId={}, type={}";
    String OUTBOX_PUBLISHED = "Событие отправлено в Kafka: eventId={}, type={}";
    String OUTBOX_PUBLISH_FAILED = "Не удалось отправить событие: eventId={}, cause={}";

    // ── Rate limit ──
    String RATE_LIMIT_HIT = "Rate limit превышен: key={}, limit={}";

    // ── Audit ──
    String AUDIT_RECORDED = "Запись аудита: action={}, actorId={}, targetId={}";
    String AUDIT_FAILED = "Не удалось записать аудит: action={}, cause={}";

    // ── JWT ──
    String JWT_INVALID = "Невалидный JWT: {}";
    String JWT_EXPIRED = "JWT просрочен: {}";

    // ── Startup ──
    String JWT_SECRET_TOO_SHORT = "JWT secret короче 32 символов — приложение не запущено";
}
