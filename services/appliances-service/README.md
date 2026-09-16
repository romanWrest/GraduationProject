# appliances-service

Микросервис учёта личных электроприборов жильцов общежития: регистрация прибора жильцом, одобрение/отклонение/отзыв администратором, контроль суммарной мощности на комнату. Публикует доменные события в Kafka по паттерну **Outbox**.

- **Порт:** `8084`
- **Схема БД:** `appliances` (в единой БД `dormitory`)
- **Root package:** `ru.dstu.dormitory.appliances_service`
- **Стек:** Spring Boot 3.3.5 · Java 21 · PostgreSQL 17 · Liquibase · Spring Data JPA · MapStruct · Spring Security (JWT) · Spring Cloud OpenFeign + Resilience4j · Spring Kafka · Redis

---

## Архитектура

```
        ┌──────────────────┐
        │ residents-service │◀─── Feign (X-Service-Token) + CircuitBreaker + Redis (TTL 5 мин)
        └──────────────────┘
                 ▲
                 │ при регистрации: получить residentId/roomId по userId
                 │
┌────────────────┴────────────────┐
│      appliances-service         │
│                                 │      ┌──────────────┐
│   REST /api/v1/appliances/...   │─────▶│  PostgreSQL  │  schema=appliances
│   Service + state machine       │      └──────────────┘
│   Лимит мощности комнаты        │
│   EventPublisher → outbox       │
│                                 │      ┌──────────────┐
│   @Scheduled OutboxRelay ───────┼─────▶│    Kafka     │  appliance.events
│                                 │      │              │
│   Kafka consumer (idempotency)  │◀─────┤              │  resident.events
└─────────────────────────────────┘      └──────────────┘
```

Жизненный цикл прибора:
```
PENDING → APPROVED → REVOKED
       ↘ REJECTED
```

При одобрении проверяется суммарная мощность APPROVED-приборов в комнате + мощность одобряемого прибора ≤ лимит (по умолчанию 3500 Вт). Лимит — параметр `app.room-power-limit-watts`.

При получении `ResidentEvicted` все APPROVED-приборы выселяемого жильца автоматически переводятся в REVOKED с reason="Жилец выселен", `decision_by=null`. События `ApplianceRevoked` публикуются с `autoRevoked=true`. Идемпотентность через `processed_events`.

Outbox-паттерн гарантирует атомарность доменной записи и публикации события.

---

## Переменные окружения

| Переменная                  | По умолчанию                                  | Описание                                                              |
|-----------------------------|-----------------------------------------------|-----------------------------------------------------------------------|
| `SPRING_PROFILES_ACTIVE`    | `local`                                       | Активный профиль (`local` / `docker`)                                 |
| `DB_URL`                    | `jdbc:postgresql://localhost:5432/dormitory`  | JDBC URL PostgreSQL                                                   |
| `DB_USERNAME`               | `postgres`                                    | Имя пользователя БД                                                   |
| `DB_PASSWORD`               | `postgres`                                    | Пароль БД                                                             |
| `REDIS_HOST`                | `localhost`                                   | Хост Redis                                                            |
| `REDIS_PORT`                | `6379`                                        | Порт Redis                                                            |
| `KAFKA_BROKERS`             | `localhost:9092`                              | Список брокеров Kafka                                                 |
| `JWT_SECRET`                | dev-секрет (override!)                        | HMAC-ключ для валидации JWT (должен совпадать с auth-service)         |
| `SERVICE_TOKEN`             | dev-значение (override!)                      | Секрет межсервисных вызовов (заголовок `X-Service-Token`)             |
| `RESIDENTS_SERVICE_URL`     | `http://localhost:8083`                       | URL residents-service                                                 |
| `APP_ROOM_POWER_LIMIT_WATTS`| `3500`                                        | Лимит суммарной мощности приборов на комнату (Вт)                     |
| `OTLP_ENDPOINT`             | `http://localhost:4318/v1/traces`             | Endpoint OTLP для трейсинга                                           |

---

## Доменная модель

| Таблица              | Назначение                                                                    |
|----------------------|-------------------------------------------------------------------------------|
| `appliance`          | Прибор: residentId/userId/roomId, type, brand, model, powerWatts, status      |
| `appliance_history`  | История переходов: from_status → to_status, actor, reason, changed_at         |
| `processed_events`   | Идемпотентность Kafka-консьюмера (eventId → processedAt)                      |
| `outbox_events`      | Outbox для асинхронной отправки доменных событий в Kafka                      |

---

## API

Все эндпоинты под префиксом `/api/v1`. Авторизация — Bearer JWT. Ошибки — единый `ErrorResponseDTO`.

| Метод | Путь                                 | Доступ                       | Назначение                                       |
|-------|--------------------------------------|------------------------------|--------------------------------------------------|
| POST  | `/appliances`                        | RESIDENT                     | Зарегистрировать прибор. status = PENDING        |
| GET   | `/appliances`                        | authenticated                | Список (ADMIN — все, RESIDENT — свои). Фильтры: status, residentId, roomId, type, search |
| GET   | `/appliances/my`                     | authenticated                | Мои приборы (упрощённый алиас)                   |
| GET   | `/appliances/{id}`                   | manager или владелец         | Детали прибора                                   |
| PATCH | `/appliances/{id}/approve`           | ADMIN                        | PENDING → APPROVED, проверка лимита мощности     |
| PATCH | `/appliances/{id}/reject`            | ADMIN                        | PENDING → REJECTED `{reason}`                    |
| PATCH | `/appliances/{id}/revoke`            | ADMIN                        | APPROVED → REVOKED `{reason}`                    |
| GET   | `/appliances/by-room/{roomId}`       | ADMIN                        | APPROVED-приборы комнаты + сумма мощности        |

### Internal

| Метод | Путь                                              | Доступ        | Назначение                          |
|-------|---------------------------------------------------|---------------|-------------------------------------|
| GET   | `/internal/appliances/{id}`                       | SERVICE/ADMIN | Прибор по id (S2S)                  |
| GET   | `/internal/appliances/by-room/{roomId}`           | SERVICE/ADMIN | Приборы комнаты с суммарной мощностью (S2S) |

---

## Бизнес-флоу

### Регистрация (RESIDENT)
1. `POST /appliances` от жильца с типом, мощностью и опциональным фото.
2. `appliances-service` обращается к residents-service (Feign + Redis-кеш `appl:resident:{userId}`, TTL 5 мин), берёт `residentId`/`roomId`. При недоступности — `503 ResidentsServiceUnavailableException`.
3. Создаётся прибор со статусом `PENDING`.
4. В `outbox_events` пишется `ApplianceRegistered`.

### Одобрение (ADMIN)
1. `PATCH /appliances/{id}/approve`.
2. State machine проверяет `PENDING → APPROVED`. Иначе → `409 IllegalApplianceTransitionException`.
3. `RoomPowerService.requireWithinLimit(roomId, powerWatts)` — суммирует APPROVED по комнате + мощность одобряемого. Если > лимита → `409 RoomPowerLimitExceededException`.
4. Статус → `APPROVED`, фиксируется `decision_by`/`decision_at`, история, `ApplianceApproved` в outbox.

### Отклонение / отзыв (ADMIN)
- `reject`: `PENDING → REJECTED` + `ApplianceRejected`.
- `revoke`: `APPROVED → REVOKED` + `ApplianceRevoked` (autoRevoked=false).

### Авто-снятие при выселении
1. residents-service публикует `ResidentEvicted` в `resident.events`.
2. `ResidentEventsConsumer` идемпотентно (по `eventId` в `processed_events`).
3. Все APPROVED-приборы жильца → `REVOKED` reason="Жилец выселен", `decision_by=null`.
4. По каждому → событие `ApplianceRevoked` с `autoRevoked=true`.

---

## События Kafka

**Publisher (`appliance.events`):**
- `ApplianceRegistered` — `{ applianceId, residentId, userId, roomId, type, powerWatts }`
- `ApplianceApproved` — `{ applianceId, residentId, decisionBy, decisionAt }`
- `ApplianceRejected` — `{ applianceId, residentId, reason, decisionBy }`
- `ApplianceRevoked` — `{ applianceId, residentId, reason, decisionBy, autoRevoked }`

**Consumer (`resident.events`):**
- `ResidentEvicted` — авто-revoke APPROVED-приборов жильца.

Стандартный envelope:
```json
{
  "eventId": "...",
  "eventType": "ApplianceRegistered",
  "aggregateType": "appliance",
  "aggregateId": "...",
  "occurredAt": "2026-04-30T...",
  "payload": { ... }
}
```

---

## Запуск

### Локально
```bash
mvn -DskipTests package
SPRING_PROFILES_ACTIVE=local java -jar target/appliances-service.jar
```

### В docker-compose
```bash
cd docker
docker compose up -d appliances-service
```

Swagger: `http://localhost:8084/swagger-ui.html`. Health: `/actuator/health`.

---

## Примеры curl

Регистрация прибора (RESIDENT):
```bash
curl -X POST http://localhost:8084/api/v1/appliances \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "KETTLE",
    "brand": "Bosch",
    "model": "TWK-3A013",
    "powerWatts": 1700,
    "notes": "Личный электрочайник"
  }'
```

Список своих приборов (RESIDENT):
```bash
curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8084/api/v1/appliances/my
```

Список всех приборов с фильтром по статусу (ADMIN):
```bash
curl -H "Authorization: Bearer $TOKEN" \
     "http://localhost:8084/api/v1/appliances?status=PENDING"
```

Одобрить прибор (ADMIN):
```bash
curl -X PATCH http://localhost:8084/api/v1/appliances/$ID/approve \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"comment":"OK, в пределах лимита"}'
```

Отклонить (ADMIN):
```bash
curl -X PATCH http://localhost:8084/api/v1/appliances/$ID/reject \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"reason":"Превышает допустимую мощность для общежития"}'
```

Снять с учёта (ADMIN):
```bash
curl -X PATCH http://localhost:8084/api/v1/appliances/$ID/revoke \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"reason":"Нарушение правил пожарной безопасности"}'
```

Приборы и суммарная мощность комнаты (ADMIN):
```bash
curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8084/api/v1/appliances/by-room/$ROOM_ID
```

Internal (S2S):
```bash
curl -H "X-Service-Token: $SERVICE_TOKEN" \
     http://localhost:8084/api/v1/internal/appliances/by-room/$ROOM_ID
```

---

## Безопасность

- `JwtAuthFilter`: валидирует подпись, извлекает `userId` и роли.
- `ServiceTokenFilter`: пропускает `/internal/**` по `X-Service-Token` либо по роли `ADMIN`.
- Stateless, CORS настраивается через `security.cors.*`.
- На уровне контроллеров — `@PreAuthorize("hasRole('RESIDENT')")` для регистрации, `@PreAuthorize("hasRole('ADMIN')")` для approve/reject/revoke и `by-room`.
- В `GET /appliances` без роли ADMIN видны только свои приборы — фильтр по `userId` принудительный.

---

## Тесты

- **Unit:**
  - `ApplianceStateMachineTest` — все валидные/невалидные переходы.
  - `ApplianceServiceTest` — регистрация, approve/reject/revoke, проверка лимита, auto-revoke.
  - `RoomPowerServiceTest` — расчёт суммы, проверка лимита.

```bash
mvn clean verify
```

Покрытие пакета `service.Impl` ≥ 70% проверяется JaCoCo.
