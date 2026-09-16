# consumables-service

Микросервис учёта расходных материалов общежития: справочник типов (постельное бельё, покрывала, подушки, полотенца), движение склада и выдача расходников жильцам с возвратом. Публикует доменные события в Kafka по паттерну **Outbox**.

- **Порт:** `8085`
- **Схема БД:** `consumables` (в единой БД `dormitory`)
- **Root package:** `ru.dstu.dormitory.consumables_service`
- **Стек:** Spring Boot 3.3.5 · Java 21 · PostgreSQL 17 · Liquibase · Spring Data JPA · MapStruct · Spring Security (JWT) · Spring Cloud OpenFeign + Resilience4j · Spring Kafka · Redis

---

## Архитектура

```
        ┌──────────────────┐
        │ residents-service │◀─── Feign (X-Service-Token) + CircuitBreaker + Redis (TTL 5 мин)
        └──────────────────┘
                 ▲
                 │ проверка существования жильца перед выдачей
                 │
┌────────────────┴────────────────┐
│      consumables-service        │
│                                 │      ┌──────────────┐
│   REST /api/v1/consumables/...  │─────▶│  PostgreSQL  │  schema=consumables
│   Service + domain              │      └──────────────┘
│   Pessimistic lock на тип       │
│   EventPublisher → outbox       │
│                                 │      ┌──────────────┐
│   @Scheduled OutboxRelay ───────┼─────▶│    Kafka     │  consumable.events
│                                 │      │              │  notification.events
│   Kafka consumer (idempotency)  │◀─────┤              │  resident.events
└─────────────────────────────────┘      └──────────────┘
```

При выдаче и пополнении — pessimistic lock (`SELECT ... FOR UPDATE`) на строке `consumable_type`. Это даёт детерминированное поведение для конкурентных выдач последнего экземпляра: один поток получает успех, другой — `OutOfStockException` (HTTP 409).

Outbox-паттерн гарантирует атомарность доменной записи и публикации события: в одной транзакции пишется строка в `consumables.outbox_events`, шедулер `OutboxRelay` вычитывает неотправленные и публикует в Kafka, проставляя `sent_at`.

---

## Переменные окружения

| Переменная               | По умолчанию                                  | Описание                                                                |
|--------------------------|-----------------------------------------------|-------------------------------------------------------------------------|
| `SPRING_PROFILES_ACTIVE` | `local`                                       | Активный профиль (`local` / `docker`)                                   |
| `DB_URL`                 | `jdbc:postgresql://localhost:5432/dormitory`  | JDBC URL PostgreSQL                                                     |
| `DB_USERNAME`            | `postgres`                                    | Имя пользователя БД                                                     |
| `DB_PASSWORD`            | `postgres`                                    | Пароль БД                                                               |
| `REDIS_HOST`             | `localhost`                                   | Хост Redis                                                              |
| `REDIS_PORT`             | `6379`                                        | Порт Redis                                                              |
| `KAFKA_BROKERS`          | `localhost:9092`                              | Список брокеров Kafka                                                   |
| `JWT_SECRET`             | dev-секрет (override!)                        | HMAC-ключ для валидации JWT (должен совпадать с auth-service)           |
| `SERVICE_TOKEN`          | dev-значение (override!)                      | Секрет межсервисных вызовов (заголовок `X-Service-Token`)               |
| `RESIDENTS_SERVICE_URL`  | `http://localhost:8083`                       | URL residents-service                                                   |
| `OTLP_ENDPOINT`          | `http://localhost:4318/v1/traces`             | Endpoint OTLP для трейсинга                                             |

---

## Доменная модель

| Таблица                | Назначение                                                                  |
|------------------------|-----------------------------------------------------------------------------|
| `consumable_type`      | Справочник типов расходников: name, unit (PIECE/SET), stock, lowStockThreshold |
| `consumable_issue`     | Выдача жильцу: residentId/userId, type, quantity, status (ISSUED/RETURNED), return_condition |
| `stock_movement`       | Журнал движений склада: typeId, delta, reason, actorId                      |
| `processed_events`     | Идемпотентность Kafka-консьюмера (eventId → processedAt)                    |
| `outbox_events`        | Outbox для асинхронной отправки доменных событий в Kafka                    |

---

## API

Все эндпоинты под префиксом `/api/v1`. Авторизация — Bearer JWT. Ошибки — единый `ErrorResponseDTO`.

### Справочник типов

| Метод | Путь                                  | Доступ                       | Назначение                                       |
|-------|---------------------------------------|------------------------------|--------------------------------------------------|
| GET   | `/consumables/types`                  | authenticated                | Получить все типы                                |
| POST  | `/consumables/types`                  | PROPERTY_MANAGER, ADMIN      | Создать тип `{name, unit, stock, lowStockThreshold?}` |
| PATCH | `/consumables/types/{id}`             | PROPERTY_MANAGER, ADMIN      | Обновить `{name?, lowStockThreshold?}`           |
| POST  | `/consumables/types/{id}/stock`       | PROPERTY_MANAGER, ADMIN      | Изменение остатка `{delta, reason}` (>0 — приход, <0 — расход) |
| GET   | `/consumables/stock`                  | PROPERTY_MANAGER, ADMIN      | Текущие остатки + флаг `low` для позиций ≤ threshold |

### Выдачи

| Метод | Путь                                          | Доступ                       | Назначение                                       |
|-------|-----------------------------------------------|------------------------------|--------------------------------------------------|
| POST  | `/consumables/issues`                         | PROPERTY_MANAGER, ADMIN      | Создать выдачу `{residentId, typeId, quantity, notes?}` |
| GET   | `/consumables/issues`                         | PROPERTY_MANAGER, ADMIN      | Поиск выдач (фильтры: residentId, typeId, status, from, to) |
| GET   | `/consumables/issues/{id}`                    | manager или сам жилец        | Детали выдачи                                    |
| PATCH | `/consumables/issues/{id}/return`             | PROPERTY_MANAGER, ADMIN      | Возврат `{condition: OK\|DAMAGED\|LOST, notes?}` |
| GET   | `/consumables/by-resident/{residentId}`       | manager или сам жилец        | Активные выдачи жильца                           |
| GET   | `/consumables/my`                             | RESIDENT                     | Мои активные выдачи                              |

### Internal

| Метод | Путь                                                    | Доступ        | Назначение                       |
|-------|---------------------------------------------------------|---------------|----------------------------------|
| GET   | `/internal/consumables/by-resident/{residentId}`        | SERVICE/ADMIN | Активные выдачи (S2S)            |

---

## Бизнес-флоу

### Выдача

1. Запрос `POST /consumables/issues` от PROPERTY_MANAGER.
2. Сервис обращается к residents-service (Feign + Redis-кеш `cons:resident:{id}`, TTL 5 мин), проверяя жильца. Если недоступен — `503 ResidentsServiceUnavailableException`.
3. `StockService.adjustStock(typeId, -quantity, "Выдача жильцу", actorId)` — pessimistic lock + проверка остатка. При нехватке → `409 OutOfStockException`.
4. Создаётся `ConsumableIssue (status=ISSUED)`, фиксируется движение в `stock_movement`.
5. В рамках той же транзакции в outbox пишется событие `ConsumableIssued`.
6. Если новый stock падает на/ниже threshold — публикуется `ConsumableStockLow`. Если до 0 — `ConsumableStockOut`.

### Возврат

1. Запрос `PATCH /consumables/issues/{id}/return` с `condition`:
   - `OK` или `DAMAGED` → stock увеличивается (DAMAGED помечается в notes/condition);
   - `LOST` → stock не меняется (расходник утерян).
2. Статус выдачи → `RETURNED`, фиксируется returnedAt/returnedBy/condition.
3. В outbox пишется `ConsumableReturned`.

### Выселение жильца

1. residents-service публикует `ResidentEvicted` в `resident.events`.
2. `ResidentEventsConsumer` обрабатывает событие идемпотентно (`processed_events` по eventId).
3. Все активные выдачи жильца → событие `ConsumableReturnRequired` в `notification.events` (notifications-service уведомит PROPERTY_MANAGER).
4. **Сами выдачи не закрываются** — управляющий должен вручную принять возврат.

---

## События Kafka

**Publisher (`consumable.events`):**
- `ConsumableIssued` — `{ issueId, residentId, userId, typeId, typeName, quantity, issuedBy }`
- `ConsumableReturned` — `{ issueId, residentId, userId, typeId, condition, returnedBy }`
- `ConsumableStockLow` — `{ typeId, name, stock, threshold }`
- `ConsumableStockOut` — `{ typeId, name }`

**Publisher (`notification.events`):**
- `ConsumableReturnRequired` — `{ residentId, userId, issueIds: [...] }`

**Consumer (`resident.events`):**
- `ResidentEvicted` — публикует `ConsumableReturnRequired` для активных выдач.

Все события заворачиваются в стандартный envelope:
```json
{
  "eventId": "...",
  "eventType": "ConsumableIssued",
  "aggregateType": "consumable_issue",
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
SPRING_PROFILES_ACTIVE=local java -jar target/consumables-service.jar
```

### В docker-compose
```bash
cd docker
docker compose up -d consumables-service
```

Swagger: `http://localhost:8085/swagger-ui.html`. Health: `/actuator/health`.

---

## Примеры curl

Получить все типы:
```bash
curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8085/api/v1/consumables/types
```

Создать новый тип (PROPERTY_MANAGER):
```bash
curl -X POST http://localhost:8085/api/v1/consumables/types \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Наматрасник","unit":"PIECE","stock":20,"lowStockThreshold":3}'
```

Пополнить остаток на 50 единиц:
```bash
curl -X POST http://localhost:8085/api/v1/consumables/types/$TYPE_ID/stock \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"delta":50,"reason":"Поставка от поставщика"}'
```

Выдать постельный комплект жильцу:
```bash
curl -X POST http://localhost:8085/api/v1/consumables/issues \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"residentId":"...","typeId":"11111111-1111-1111-1111-111111111111","quantity":1,"notes":"При заселении"}'
```

Принять возврат:
```bash
curl -X PATCH http://localhost:8085/api/v1/consumables/issues/$ISSUE_ID/return \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"condition":"OK","notes":"Чистое, без повреждений"}'
```

Возврат повреждённого:
```bash
curl -X PATCH http://localhost:8085/api/v1/consumables/issues/$ISSUE_ID/return \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"condition":"DAMAGED","notes":"Прожог на наволочке"}'
```

Списать как утерянное (stock не возрастёт):
```bash
curl -X PATCH http://localhost:8085/api/v1/consumables/issues/$ISSUE_ID/return \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"condition":"LOST"}'
```

Активные выдачи у жильца:
```bash
curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8085/api/v1/consumables/by-resident/$RESIDENT_ID
```

Свои выдачи (RESIDENT):
```bash
curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8085/api/v1/consumables/my
```

Текущий склад с пометкой low:
```bash
curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8085/api/v1/consumables/stock
```

Internal (S2S):
```bash
curl -H "X-Service-Token: $SERVICE_TOKEN" \
     http://localhost:8085/api/v1/internal/consumables/by-resident/$RESIDENT_ID
```

---

## Безопасность

- JWT-фильтр (`JwtAuthFilter`): валидирует подпись, извлекает `userId` и роли из access-токена.
- `ServiceTokenFilter`: пропускает `/internal/**` по заголовку `X-Service-Token` либо по роли `ADMIN`.
- Stateless. CORS настраивается через `security.cors.*`.
- На уровне контроллеров — `@PreAuthorize` для роли PROPERTY_MANAGER/ADMIN на мутации.

---

## Тесты

- **Unit** (`StockServiceTest`, `ConsumableIssueServiceTest`) — выдача/возврат с разными condition, защитные ветки.
- **Integration** (`ConcurrentIssuanceIT`) — Testcontainers PostgreSQL: два потока конкурентно выдают последний экземпляр, один получает успех, второй — `OutOfStockException`. Подтверждает корректность pessimistic lock.

```bash
mvn clean verify
```

Покрытие пакета `service.Impl` ≥ 70% проверяется JaCoCo.
