# residents-service

Микросервис управления жильцами общежития: комнаты, заселение/выселение/переселение, история проживания, инвентарь. Публикует доменные события в Kafka по паттерну **Outbox**.

- **Порт:** `8083`
- **Схема БД:** `residents` (в единой БД `dormitory`)
- **Root package:** `ru.dstu.dormitory.residents_service`
- **Стек:** Spring Boot 3.3.5 · Java 21 · PostgreSQL 16 · Liquibase · Spring Data JPA · MapStruct · Spring Security (JWT) · Spring Cloud OpenFeign + Resilience4j · Spring Kafka

---

## Архитектура

```
        ┌──────────────┐
        │ auth-service │◀─── Feign (X-Service-Token) + CircuitBreaker + fallback
        └──────────────┘
               ▲
               │ верификация userId/ролей
               │
┌──────────────┴──────────────┐
│      residents-service       │
│                              │      ┌──────────────┐
│   REST /api/v1/...           │─────▶│  PostgreSQL  │  schema=residents
│   Service + domain           │      └──────────────┘
│   EventPublisher → outbox    │
│                              │      ┌──────────────┐
│   @Scheduled OutboxRelay ────┼─────▶│    Kafka     │  topic=resident.events
│                              │      └──────────────┘
└──────────────────────────────┘
```

Outbox-паттерн гарантирует атомарность доменной записи и публикации события: в одной транзакции с обновлением доменных таблиц пишется строка в `residents.outbox_events`, а отдельный шедулер `OutboxRelay` вычитывает неотправленные события и публикует их в Kafka, после чего проставляет `sent_at`.

---

## Переменные окружения

| Переменная            | Значение по умолчанию                         | Описание                                               |
|-----------------------|-----------------------------------------------|--------------------------------------------------------|
| `SPRING_PROFILES_ACTIVE` | `local`                                    | Активный профиль (`local` / `docker` / `integration`)  |
| `DB_URL`              | `jdbc:postgresql://localhost:5432/dormitory`  | JDBC URL PostgreSQL                                    |
| `DB_USERNAME`         | `postgres`                                    | Имя пользователя БД                                    |
| `DB_PASSWORD`         | `postgres`                                    | Пароль БД                                              |
| `REDIS_HOST`          | `localhost`                                   | Хост Redis                                             |
| `REDIS_PORT`          | `6379`                                        | Порт Redis                                             |
| `KAFKA_BROKERS`       | `localhost:9092`                              | Список брокеров Kafka                                  |
| `JWT_SECRET`          | dev-секрет (override!)                        | HMAC-ключ для валидации JWT (должен совпадать с auth)  |
| `SERVICE_TOKEN`       | dev-значение (override!)                      | Секрет для межсервисных вызовов (заголовок `X-Service-Token`) |
| `AUTH_SERVICE_URL`    | `http://localhost:8081`                       | URL auth-service для Feign-клиента                     |
| `CORS_ORIGINS`        | `http://localhost:3000,http://localhost:5173` | Разрешённые origin для CORS                            |

---

## Запуск

### Локально (мой стенд docker-compose уже поднят)

```bash
cd C:\Users\Roman\Desktop\diplom\dev\docker
docker compose up -d postgres redis kafka auth-service

cd ..\services\residents-service
./mvnw spring-boot:run
```

Swagger UI: http://localhost:8083/swagger-ui.html
OpenAPI JSON: http://localhost:8083/v3/api-docs

### В Docker

```bash
cd C:\Users\Roman\Desktop\diplom\dev\services\residents-service
./mvnw -DskipTests package
cd ..\..\docker
docker compose up -d residents-service
```

### Тесты

```bash
./mvnw clean verify
```

Unit-тесты (30 шт.) покрывают сервисный слой (Room/Resident/Inventory), мапперы и Outbox-relay.

---

## REST API

Базовый путь: `/api/v1`. Все мутации требуют роль `ADMIN`; чтения доступны любому аутентифицированному пользователю. Внутренние эндпоинты (`/api/v1/internal/**`) — только для сервисов с валидным `X-Service-Token` или ADMIN.

### Комнаты — `/api/v1/rooms`
| Метод | Путь | Роль | Описание |
|------|------|------|----------|
| GET  | `/rooms`              | authenticated | Список комнат с пагинацией |
| GET  | `/rooms/{id}`         | authenticated | Детальный DTO (комната + текущие жильцы + инвентарь) |
| GET  | `/rooms/{id}/residents` | authenticated | Активные жильцы комнаты |
| GET  | `/rooms/{id}/history` | authenticated | История заселений комнаты |
| POST | `/rooms`              | ADMIN         | Создать комнату |
| PATCH | `/rooms/{id}`        | ADMIN         | Частичное обновление (нельзя уменьшить `capacity` ниже текущего числа жильцов) |
| DELETE | `/rooms/{id}`       | ADMIN         | Удалить комнату (если нет активных жильцов) |

### Жильцы — `/api/v1/residents`
| Метод | Путь | Роль | Описание |
|------|------|------|----------|
| GET  | `/residents`        | authenticated | Поиск с фильтрами (`kind`, `roomId`, `faculty`, `active`) + пагинация |
| GET  | `/residents/{id}`   | authenticated | Один жилец |
| GET  | `/residents/{id}/history` | authenticated | История заселений жильца |
| POST | `/residents`        | ADMIN | Заселение (создаёт `Resident` + `ResidencyHistory` + событие `ResidentEnrolled`) |
| PATCH | `/residents/{id}`  | ADMIN или владелец | Обновить контакты/доп. поля (владелец может менять только `phone`/`contactInfo`) |
| POST | `/residents/{id}/evict` | ADMIN | Выселение (закрывает историю + `ResidentEvicted`) |
| POST | `/residents/{id}/move`  | ADMIN | Переселение (закрывает текущую строку истории, открывает новую + `ResidentMoved`) |

### Инвентарь — `/api/v1/rooms/{roomId}/inventory` + `/api/v1/inventory/{id}`
| Метод | Путь | Роль | Описание |
|------|------|------|----------|
| GET   | `/rooms/{roomId}/inventory` | authenticated | Инвентарь комнаты |
| POST  | `/rooms/{roomId}/inventory` | ADMIN | Создать позицию инвентаря |
| GET   | `/inventory/{id}`           | authenticated | Одна позиция |
| PATCH | `/inventory/{id}`           | ADMIN | Обновить состояние/заметки (нельзя трогать `WRITTEN_OFF` позиции) |
| POST  | `/inventory/{id}/write-off` | ADMIN | Списать позицию |
| DELETE | `/inventory/{id}`          | ADMIN | Удалить (только `WRITTEN_OFF`) |

### Internal — `/api/v1/internal/**` (SERVICE / ADMIN)
| Метод | Путь | Описание |
|------|------|----------|
| POST | `/internal/residents/batch` | Получить жильцов по списку `userIds` (для других сервисов) |

---

## Kafka-события

Все события публикуются в топик **`resident.events`** (3 партиции, партиционирование по `residentId`). Формат — JSON-envelope:

```json
{
  "eventId": "uuid",
  "eventType": "ResidentEnrolled",
  "aggregateType": "resident",
  "aggregateId": "<residentId>",
  "occurredAt": "2026-04-22T08:15:30Z",
  "payload": { ... }
}
```

| `eventType`         | Когда публикуется                | payload                                                                      |
|---------------------|----------------------------------|------------------------------------------------------------------------------|
| `ResidentEnrolled`  | `POST /residents`                | `residentId`, `userId`, `kind`, `roomId`, `enrolledAt`                       |
| `ResidentEvicted`   | `POST /residents/{id}/evict`     | `residentId`, `userId`, `roomId`, `evictedAt`, `reason`                      |
| `ResidentMoved`     | `POST /residents/{id}/move`      | `residentId`, `userId`, `fromRoomId`, `toRoomId`, `movedAt`, `reason`        |

---

## Примеры (curl)

```bash
# Логин в auth-service и получение access-токена
TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@example.com","password":"Admin12345!"}' | jq -r .accessToken)

# Создать комнату
curl -X POST http://localhost:8083/api/v1/rooms \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"number":"305","floor":3,"capacity":2,"notes":"угловая"}'

# Заселить студента
curl -X POST http://localhost:8083/api/v1/residents \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"userId":"<uuid>","kind":"STUDENT","faculty":"ФИТ","studyGroup":"ИТ-31","phone":"+7...","roomId":"<uuid>","enrolledAt":"2026-09-01"}'

# Выселить
curl -X POST http://localhost:8083/api/v1/residents/<id>/evict \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"evictedAt":"2027-06-30","reason":"окончание учёбы"}'
```

---

## Структура

```
src/main/java/ru/dstu/dormitory/residents_service
├── client/             — Feign-клиент к auth-service + fallback + service-token interceptor
├── config/             — SecurityConfig, KafkaConfig, конфиги под конкретные секции
├── domain/
│   ├── enums/          — ResidentKind, InventoryType, InventoryState
│   ├── model/          — Room, Resident, ResidencyHistory, InventoryItem, OutboxEvent
│   └── repo/           — Spring Data репозитории
├── exception/          — кастомные исключения + GlobalExceptionHandler (ErrorResponseDTO)
├── mapper/             — MapStruct мапперы
├── security/           — JwtAuthFilter, ServiceTokenFilter, ResidentAccess (для SpEL)
├── service/
│   ├── Impl/           — реализации бизнес-сервисов
│   └── event/          — EventPublisher, OutboxRelay, event records, EventTypes
├── util/               — LogMethod, LogPatterns
└── web/                — REST-контроллеры + DTO
```

Liquibase-миграции: `src/main/resources/db/changelog/` (YAML).
