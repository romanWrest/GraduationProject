package ru.dstu.dormitory.notifications_service.util;

/**
 * Константы строк логов для SLF4J. Шаблоны используют {}-плейсхолдеры или %s для String.format.
 */
@SuppressWarnings("unused")
public interface LogPatterns {

    // ── Validation ──
    String UUID_PATTERN = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
    String VALIDATION_ERROR = "Ошибка валидации поле [{}, {}]: {}";

    // ── Exception handler ──
    String CLIENT_ERROR = "Клиентская ошибка [{}]: {} ({})";
    String SERVER_ERROR = "Серверная ошибка [{}]: {} ({})";
    String UNHANDLED_EXCEPTION = "Необработанное исключение";
    String INVALID_JSON_FORMAT = "Некорректный формат JSON";

    // ── Exception descriptions (String.format) ──
    String PATH_NOT_FOUND = "Путь не найден: /%s";
    String MISSING_PARAM = "Отсутствует обязательный параметр запроса: '%s' (тип: %s)";
    String MISSING_HEADER = "Отсутствует обязательный заголовок: '%s'";
    String TYPE_MISMATCH = "Параметр '%s' имеет неверный тип. Ожидается: %s";
    String METHOD_NOT_SUPPORTED = "Метод '%s' не поддерживается. Доступные методы: %s";
    String SUPPORTED_METHODS_UNDEFINED = "не определены";

    // ── Kafka consumer ──
    String EVENT_RECEIVED = "Получено событие: topic={}, type={}, eventId={}";
    String EVENT_PROCESSED = "Событие обработано: type={}, eventId={}, ms={}";
    String EVENT_SKIPPED = "Событие пропущено: type={}, eventId={}, reason={}";
    String EVENT_FAILED = "Ошибка обработки события: type={}, eventId={}, cause={}";
    String EVENT_DUPLICATE = "Дубликат события (idempotency): eventId={}";

    // ── Email ──
    String EMAIL_SENDING = "Отправка email: to={}, template={}";
    String EMAIL_SENT = "Email отправлен: to={}, template={}, ms={}";
    String EMAIL_FAILED = "Не удалось отправить email: to={}, template={}, cause={}";
    String EMAIL_SKIPPED_BY_SETTINGS = "Email не отправлен (отключён настройками): userId={}, type={}";

    // ── In-app уведомления ──
    String NOTIFICATION_CREATED = "Уведомление создано: userId={}, type={}";
    String NOTIFICATION_READ = "Уведомление прочитано: id={}, userId={}";

    // ── Шаблоны ──
    String TEMPLATE_RENDERED = "Шаблон отрендерен: name={}, ms={}";
    String TEMPLATE_NOT_FOUND = "Шаблон не найден: name={}";

    // ── Интеграции ──
    String AUTH_CLIENT_CALL = "Запрос к auth: path={}, result={}";
    String AUTH_CLIENT_FALLBACK = "Сработал fallback auth: reason={}";

    // ── JWT ──
    String JWT_INVALID = "Невалидный JWT: {}";
    String JWT_SECRET_TOO_SHORT = "JWT secret короче 32 символов — приложение не запущено";
}
