package ru.dstu.dormitory.requests_service.util;

/**
 * Константы строк логов для SLF4J.
 * Шаблоны используют {}-плейсхолдеры SLF4J либо %s для String.format.
 */
@SuppressWarnings("unused")
public interface LogPatterns {

    // ── Validation ──
    String UUID_PATTERN = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
    String UUID_MESSAGE_ERROR_PATTERN = "Ошибка формата UUID";
    String VALIDATION_ERROR = "Ошибка валидации поле [{}, {}]: {}";
    String CACHE_CHECK_ERROR = "Ошибка при проверке кэша: {}";

    // ── Exception handler ──
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

    // ── Жизненный цикл заявки ──
    String REQUEST_CREATED = "Заявка создана: id={}, type={}, authorId={}, roomId={}";
    String REQUEST_STATUS_CHANGED = "Статус заявки изменён: id={}, {} → {}";
    String REQUEST_ASSIGNED = "Заявка назначена: id={}, assigneeId={}, scheduledAt={}";
    String REQUEST_CLOSED = "Заявка закрыта: id={}, resolutionCommentLength={}";
    String REQUEST_CANCELLED = "Заявка отозвана автором: id={}";
    String REQUEST_REOPENED = "Заявка переоткрыта: id={}";
    String REQUEST_REJECTED = "Заявка отклонена: id={}, reason={}";
    String REQUEST_AUTOCLOSED = "Заявка автозакрыта: id={}, в статусе DONE более 3 дней";

    // ── Маршрутизация ──
    String REQUEST_ROUTED = "Заявка отправлена в пул: id={}, pool={}";

    // ── Комментарии и вложения ──
    String COMMENT_ADDED = "Комментарий добавлен: requestId={}, authorId={}";
    String ATTACHMENT_UPLOADED = "Вложение загружено: requestId={}, attachmentId={}, size={}, contentType={}";
    String ATTACHMENT_DELETED = "Вложение удалено: requestId={}, attachmentId={}";

    // ── Outbox / Kafka ──
    String OUTBOX_SAVED = "Событие сохранено в outbox: eventId={}, type={}";
    String OUTBOX_PUBLISHED = "Событие отправлено в Kafka: eventId={}, type={}";
    String OUTBOX_PUBLISH_FAILED = "Не удалось отправить событие: eventId={}, cause={}";

    // ── Интеграции ──
    String RESIDENTS_CLIENT_CALL = "Запрос в residents: path={}, result={}";
    String RESIDENTS_CLIENT_FALLBACK = "Сработал fallback residents: reason={}";
    String AUTH_CLIENT_FALLBACK = "Сработал fallback auth: reason={}";
    String MINIO_UPLOAD = "MinIO upload: bucket={}, key={}, size={}";
    String MINIO_DOWNLOAD = "MinIO download: bucket={}, key={}";
    String MINIO_ERROR = "Ошибка MinIO: operation={}, cause={}";
    String MINIO_BUCKET_READY = "MinIO bucket готов: {}";

    // ── JWT ──
    String JWT_INVALID = "Невалидный JWT: {}";
    String JWT_EXPIRED = "JWT просрочен: {}";

    // ── Startup ──
    String JWT_SECRET_TOO_SHORT = "JWT secret короче 32 символов — приложение не запущено";
}
