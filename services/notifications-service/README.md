# notifications-service

Микросервис уведомлений: подписывается на Kafka-события (`user.events`,
`resident.events`, `request.events`), сохраняет in-app уведомления в БД и
рассылает email через SMTP.

- **Порт:** `8087`
- **Схема БД:** `notifications` (общая БД `dormitory`, общий пользователь `postgres`)
- **Корневой пакет:** `ru.dstu.dormitory.notifications_service`

## Запуск

### В Docker Compose

```bash
cd dev/docker
docker compose up -d notifications-service mailhog
```

Сервис ждёт `postgres`, `redis`, `kafka`, `mailhog`, `auth-service` (см. `depends_on`).

### Локально

Поднять зависимости:

```bash
docker compose -f dev/docker/docker-compose.yml up -d postgres redis kafka mailhog auth-service
```

Запустить:

```bash
cd dev/services/notifications-service
./mvnw spring-boot:run
```

По умолчанию активен профиль `local`.

## Где смотреть письма

MailHog UI: <http://localhost:8025>. Все письма из локальной разработки видны там.

## Endpoint-ы

`/swagger-ui.html` — полная документация. Кратко:

| Метод | Путь | Описание |
|---|---|---|
| `GET` | `/api/v1/notifications` | Мои уведомления (с фильтрами `unreadOnly`, `type`, `from`, `to`) |
| `GET` | `/api/v1/notifications/unread-count` | Количество непрочитанных |
| `PATCH` | `/api/v1/notifications/{id}/read` | Пометить прочитанным |
| `PATCH` | `/api/v1/notifications/read-all` | Пометить все прочитанными |
| `GET` | `/api/v1/notifications/settings` | Мои настройки каналов |
| `PATCH` | `/api/v1/notifications/settings` | Обновить настройки |
| `POST` | `/api/v1/internal/notifications` | Прямая отправка (S2S, X-Service-Token) |

Примеры curl:

```bash
TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@dormitory.local","password":"Admin12345!"}' \
  | jq -r .accessToken)

# Список моих уведомлений
curl -H "Authorization: Bearer $TOKEN" http://localhost:8087/api/v1/notifications

# Количество непрочитанных
curl -H "Authorization: Bearer $TOKEN" http://localhost:8087/api/v1/notifications/unread-count

# Прямая отправка через service token
curl -X POST http://localhost:8087/api/v1/internal/notifications \
  -H 'Content-Type: application/json' \
  -H 'X-Service-Token: change-me-service-token' \
  -d '{
    "userId": "00000000-0000-0000-0000-000000000001",
    "type": "SYSTEM",
    "title": "Привет",
    "body": "Тест",
    "channels": ["IN_APP"]
  }'
```

## События и шаблоны

| Event | Шаблон | Получатели |
|---|---|---|
| `UserCreated` | `welcome.html` | пользователь |
| `UserDeactivated` | `account-deactivated.html` | пользователь |
| `PasswordResetRequested` | `password-reset.html` | пользователь |
| `ResidentEnrolled` | `resident-enrolled.html` | жилец |
| `ResidentEvicted` | `resident-evicted.html` | жилец |
| `ResidentMoved` | `resident-moved.html` | жилец |
| `RequestCreated` | `request-created.html` | автор + админы |
| `RequestAssigned` | `request-assigned.html` | автор + исполнитель |
| `RequestStatusChanged` | `request-status-changed.html` | автор |
| `RequestClosed` | `request-closed.html` | автор |

## Идемпотентность

Каждое событие имеет уникальный `eventId`. Перед обработкой проверяется
наличие в `notifications.processed_events`. Дубликаты пишутся в лог как
`Дубликат события (idempotency): eventId=...` и не приводят к дублям
писем/уведомлений.

## Retry и DLT

`DefaultErrorHandler` со схемой `1s → 3s → 10s` (3 попытки), затем сообщение
уходит в топик `notifications.dlt`. Битый JSON именно так и обрабатывается.

## Метрики и трейсинг

- `/actuator/health/readiness` — readiness probe (зависит от БД, Kafka, SMTP)
- `/actuator/prometheus` — метрики Prometheus
  - `notifications_emails_sent_total{template,status}`
  - `notifications_events_processed_total{type,result}`
  - `notifications_event_processing_seconds{type}`

> Кастомные метрики `notifications_*` собираются автоматически из аспектов
> `@LogMethod` и Micrometer-таймеров вокруг handler-ов.

## Тесты

```bash
./mvnw clean verify
```

- Unit-тесты — JUnit 5 + Mockito.
- Интеграционные (`*IT`) — Testcontainers (Postgres + Kafka), GreenMail (SMTP),
  WireMock (auth-service). Запускаются в фазе `integration-test` через
  `maven-failsafe`.

## Известные ограничения

- Эндпоинт `GET /api/v1/internal/users?role=ADMIN` в `auth-service` ещё не
  реализован — fallback в Feign-клиенте возвращает пустой список, поэтому
  уведомления администраторам о `RequestCreated` сейчас не рассылаются. После
  добавления endpoint-а никаких изменений в notifications-service не нужно.
