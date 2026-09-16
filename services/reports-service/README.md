# reports-service

Сервис аналитики и отчётности (CQRS read-модели). Финальный сервис в системе СУО.

## Что делает

- Подписан на 5 Kafka-топиков (user/resident/request/appliance/consumable events) и строит **read-модели** в схеме `reports`.
- Денормализует имена пользователей при поздних событиях (late JOIN).
- Идемпотентность через таблицу `processed_events` (вставка по `eventId`, `INSERT IGNORE` через `DataIntegrityViolation`).
- Отдаёт **5 отчётов** в JSON: requests, assignees, residents, appliances, consumables.
- Универсальный экспорт: **CSV** (UTF-8 с BOM, OpenCSV), **XLSX** (Apache POI, freeze pane, форматы), **PDF** (Thymeleaf + openhtmltopdf, альбомная A4).
- Эндпоинт `/sync-status` — диагностика: сколько событий обработано по каждому топику и текущий lag.
- DLT (`reports.dlt`) при 4 неудачных попытках обработки.

## Стек

Java 21, Spring Boot 3.3.5, Spring Kafka, Spring Data JPA, Liquibase, Apache POI 5.3, OpenCSV 5.9, openhtmltopdf 1.0.10, Thymeleaf, JJWT, Springdoc OpenAPI, Micrometer + OTLP.

## Порт и зависимости

- Слушает: **8088**.
- Требует: PostgreSQL (схема `reports`), Kafka.

## Конфигурация (env)

| Переменная | Назначение | Дефолт |
|---|---|---|
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | PostgreSQL | localhost / postgres / postgres |
| `KAFKA_BROKERS` | Kafka bootstrap | localhost:9092 |
| `JWT_SECRET` | HMAC-секрет (≥ 32 символа) — общий с auth-service | dev-секрет |
| `APP_REPORT_MAX_ROWS` | Жёсткий лимит строк в отчёте/экспорте | 100000 |
| `APP_REPORT_MAX_PERIOD_MONTHS` | Максимальная длительность периода | 24 |
| `OTLP_ENDPOINT` | OpenTelemetry collector | http://localhost:4318/v1/traces |
| `SPRING_PROFILES_ACTIVE` | `local` / `docker` | local |

## Запуск локально

```bash
# 1. PostgreSQL + Kafka из docker-compose.yml
docker compose -f ../../docker/docker-compose.yml up -d postgres kafka

# 2. Сборка и запуск
mvn clean package
java -jar target/reports-service.jar
```

## Запуск в docker-compose

```bash
docker compose -f ../../docker/docker-compose.yml up --build reports-service
```

## Эндпоинты

Все под префиксом `/api/v1/reports`. Доступ — только `ROLE_ADMIN`.

| Метод | Path | Описание |
|---|---|---|
| GET | `/requests?from=&to=&type=&status=&assigneeId=` | Отчёт по заявкам |
| GET | `/assignees?from=&to=` | Загрузка исполнителей |
| GET | `/residents?asOf=YYYY-MM-DD` | Текущие проживающие |
| GET | `/appliances?roomId=&status=` | Электроприборы |
| GET | `/consumables?from=&to=&typeId=` | Выдачи расходников |
| GET | `/{name}/export?format=csv\|xlsx\|pdf&...` | Универсальный экспорт |
| GET | `/sync-status` | Прогресс обработки топиков |

## Примеры curl

```bash
# Получить токен админа
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@dormitory.local","password":"Admin12345!"}' | jq -r .accessToken)

# Отчёт по заявкам за январь 2026
curl -H "Authorization: Bearer $TOKEN" \
  'http://localhost:8088/api/v1/reports/requests?from=2026-01-01T00:00:00Z&to=2026-02-01T00:00:00Z'

# Экспорт XLSX
curl -OJ -H "Authorization: Bearer $TOKEN" \
  'http://localhost:8088/api/v1/reports/requests/export?format=xlsx&from=2026-01-01T00:00:00Z&to=2026-02-01T00:00:00Z'

# Экспорт PDF (residents)
curl -OJ -H "Authorization: Bearer $TOKEN" \
  'http://localhost:8088/api/v1/reports/residents/export?format=pdf&asOf=2026-05-02'

# Прогресс обработки
curl -H "Authorization: Bearer $TOKEN" http://localhost:8088/api/v1/reports/sync-status
```

## Структура read-моделей

```
reports.users_view          — справочная (для денормализации имён)
reports.requests_view       — заявки + closed_at, resolution_seconds
reports.residents_view      — жильцы + enrolled_at, evicted_at
reports.appliances_view     — приборы + статус, мощность
reports.consumables_view    — выдачи + return_condition
reports.processed_events    — идемпотентность
```

Индексы под отчёты: `period`, `assignee+status`, `type+status`, `active residents`, `appliance status/room`, `consumable period/resident`.

## Архитектура обработки события

```
Kafka topic
   ↓ Kafka listener (5 consumers, group=reports-service-group, ack=MANUAL_IMMEDIATE)
   ↓ KafkaEventDispatcher: parse envelope → idempotency check → handler
   ↓ HandlerRegistry: resolve by eventType
   ↓ EventHandler.handle(payload): upsert read-модели
   ↓ IdempotencyService.markProcessed(eventId)
   ↓ ack.acknowledge()
```

При исключении → 3 retry с FixedBackOff(2с) → DLT (`reports.dlt`).

При получении `UserCreated` — `LateJoinService` дозаполняет `author_name` / `assignee_name` / `resident_name` во всех read-моделях по `userId`.

## Тесты

```bash
mvn test
```

Юнит:
- `RequestCreatedHandlerTest`, `RequestClosedHandlerTest`, `UserCreatedHandlerTest`.
- `RequestsReportServiceTest` — агрегация и валидация периода.
- `CsvExporterTest`, `XlsxExporterTest`, `PdfExporterTest`.

Integration-тесты (Testcontainers PostgreSQL+Kafka) и e2e-тесты экспорта/idempotency не реализованы — требуют поднятого Docker; можно запустить вручную через `docker compose up`.

## Метрики и логи

- `/actuator/prometheus` — стандартные метрики Spring + Kafka consumer.
- `/actuator/health` (с probes liveness/readiness).
- Логи: `[traceId,spanId] level logger - msg`. JSON-encoder для Loki в `logback-spring.xml` закомментирован — включить вместе с Loki в стеке.

## Известные ограничения

- Eureka не используется (в docker-compose нет) — все downstream-обращения отсутствуют, сервис работает только на Kafka-событиях.
- PDF: используется единый универсальный шаблон `templates/reports/table.html`. Стилизованный шаблон под каждый отчёт можно добавить отдельно.
- OGNL не на classpath: `PdfExporterTest` явно создаёт `SpringTemplateEngine` (использует SpEL вместо OGNL).
