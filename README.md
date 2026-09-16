# Система управления общежитием (СУО)

Дипломный проект — микросервисная информационная система для управления
общежитием ВУЗа. Покрывает учёт жильцов и комнат, заявки на обслуживание,
учёт личных электроприборов, выдачу расходных материалов, уведомления и
аналитическую отчётность.

Backend — набор микросервисов на **Java 21 / Spring Boot 3.3.5**, единый вход
через **API Gateway**. Frontend — **React 18 + TypeScript** SPA. Инфраструктура
(PostgreSQL, Redis, Kafka, MinIO, MailHog) поднимается через Docker Compose.

> Проект полностью спроектирован и реализован через **Claude Code**, без ручного
> написания серверного кода. Как именно был построен процесс — в разделе
> [«Как разрабатывался проект»](#как-разрабатывался-проект).

---

## Содержание

- [Система управления общежитием (СУО)](#система-управления-общежитием-суо)
  - [Содержание](#содержание)
  - [Как разрабатывался проект](#как-разрабатывался-проект)
  - [Архитектура](#архитектура)
  - [Состав сервисов](#состав-сервисов)
  - [Технологический стек](#технологический-стек)
  - [Инфраструктура и порты](#инфраструктура-и-порты)
  - [Модель данных](#модель-данных)
  - [Обмен событиями (Kafka)](#обмен-событиями-kafka)
  - [Аутентификация и роли](#аутентификация-и-роли)
  - [Быстрый старт](#быстрый-старт)
    - [1. Всё в Docker Compose](#1-всё-в-docker-compose)
    - [2. Гибридный режим (инфраструктура в Docker, сервис локально)](#2-гибридный-режим-инфраструктура-в-docker-сервис-локально)
    - [3. Frontend (dev)](#3-frontend-dev)
    - [Проверка](#проверка)
  - [Демо-данные (seed)](#демо-данные-seed)
  - [Наблюдаемость](#наблюдаемость)
  - [Тесты](#тесты)
  - [Структура репозитория](#структура-репозитория)

---

## Как разрабатывался проект

Проект написан через Claude Code (Opus, Май-Июнь 2026), без ручного
написания серверного кода. Процесс:

1. **ТЗ.** Сначала было составлено техническое задание в docx с описанием
   предметной области и функциональных требований.
2. **Архитектура.** В сессии с Claude Code продумана микросервисная
   архитектура: состав сервисов, границы доменов, БД, способ межсервисного
   взаимодействия (REST + Kafka).
3. **Контракт API.** Отдельным шагом составлен полный список эндпоинтов всех
   сервисов — как контракт между микросервисами и фронтендом, зафиксированный
   до начала реализации.
4. **Промпты по сервисам.** Для каждого микросервиса был написан отдельный
   промпт на основе контракта и архитектуры.
5. **Реализация — отдельная сессия на сервис.** Каждый микросервис
   разрабатывался в своей сессии Claude Code, без смешивания контекста между
   сервисами.
6. **Проверка и исправление ошибок — отдельный цикл.**
   - Сессия №1: запуск сервиса и проверка на логические ошибки; при
     обнаружении проблем агент составлял `ERROR_REPORT.md` с их описанием.
   - Отдельно, в новом окне: составление промпта на исправление по этому
     отчёту.
   - Отдельная сессия: запуск промпта-исправления.
   - Повторная проверка результата.

Таким образом на каждый микросервис приходилось до 4 отдельных сессий с
разными задачами (реализация → тестирование → составление фикс-промпта →
исправление), без потери контекста внутри одной сессии.

**Эталонный сервис — написан руками.** В основе архитектуры всех микросервисов
лежит референсный сервис, который был написан вручную, без агента и успешно обслуживает более 100 тыс человек ежедневно. Именно его
структура — слои, границы домена, работа с БД и Kafka, паттерны Transactional
Outbox и идемпотентных консьюмеров — стала образцом, по которому через Claude
Code разрабатывались остальные сервисы. Поэтому у всех сервисов единая,
понятная до кода архитектура, а не «как получилось у агента»: агент повторял
уже осмысленный и проверенный руками паттерн, а не изобретал его заново.

**Экономия токенов — граф кодовой базы (Graphify).** Поверх кода строится граф
через Graphify (скилл `/graphify` для Claude Code): узлы — функции, классы и
методы, рёбра — вызовы, импорты и наследование, разобранные локально через
tree-sitter AST. Агент запрашивает граф (`query` / `path` / `explain`) и
получает точечный подграф вместо чтения файлов целиком — в контекст попадают
только нужные узлы и их связи. По ощущениям это заметно сократило расход
токенов в сессиях — порядка 45-60% (субъективная оценка, точных замеров пока нет).


**Фронтенд, отдельно.** Бэкенд-процесс выше не был в полной мере
повторён на фронтенде: из-за сроков и ограниченного токен-бюджета (без
подписки Max) большая часть страниц фронтенда переписывалась вручную,
агент использовался точечно, без отдельного цикла тестирования и
фикс-промптов, как на бэкенде.

---

## Архитектура

Система построена по принципу **микросервисов** с чёткими границами доменов.
Каждый сервис владеет собственной схемой в общей БД `dormitory`, общается с
остальными двумя способами:

- **синхронно** — через REST (межсервисные вызовы Feign с заголовком
  `X-Service-Token`, защищённые Circuit Breaker'ом Resilience4j и Redis-кешем);
- **асинхронно** — через **Apache Kafka** по паттерну **Transactional Outbox**
  (доменная запись и событие пишутся в одной транзакции, отдельный шедулер
  `OutboxRelay` публикует их в Kafka). Консьюмеры идемпотентны (таблица
  `processed_events` по `eventId`).

```
                        ┌───────────────┐
        Браузер ───────▶│  API Gateway  │  :8080  (JWT, CORS, rate limit)
       (React SPA)      └───────┬───────┘
                                │  проброс X-User-Id / X-User-Roles
        ┌───────────────┬───────┼────────────┬───────────────┬──────────────┐
        ▼               ▼       ▼            ▼               ▼              ▼
   auth-service   residents  requests   appliances    consumables   notifications   reports
     :8081         :8083      :8082       :8084          :8085          :8087        :8088
        │               │       │            │               │              │           │
        └───────────────┴───────┴──── Kafka (события домена) ┴──────────────┴───────────┘
                                │
        PostgreSQL 17 ── Redis ── Kafka ── MinIO ── MailHog  (инфраструктура)
```

- **Eureka / Config Server не используются** — маршрутизация Gateway идёт на
  прямые URI сервисов (`http://service-name:port`).
- HTTPS-терминирование предполагается на reverse proxy (nginx) перед Gateway.
- Валидация ролей — в самих сервисах через `@PreAuthorize`; Gateway проверяет
  только подпись и срок действия JWT.

---

## Состав сервисов

| Сервис | Порт | Схема БД | Назначение |
|---|---|---|---|
| **api-gateway** | 8080 | — | Единая точка входа: маршрутизация, валидация JWT, CORS, rate limiting (Redis token bucket), агрегированный Swagger. |
| **auth-service** | 8081 | `auth` | Аутентификация, выдача/ротация JWT (access + refresh), сброс пароля, управление пользователями, RBAC, аудит. |
| **requests-service** | 8082 | `requests` | Заявки на обслуживание: создание, назначение, статусная машина, комментарии, вложения (MinIO). |
| **residents-service** | 8083 | `residents` | Жильцы, комнаты, заселение/выселение/переселение, история проживания, инвентарь комнат. |
| **appliances-service** | 8084 | `appliances` | Учёт личных электроприборов жильцов, модерация (approve/reject/revoke), контроль лимита мощности на комнату. |
| **consumables-service** | 8085 | `consumables` | Учёт расходников (бельё, полотенца и т.п.): склад, выдача жильцам, возврат. |
| **notifications-service** | 8087 | `notifications` | In-app уведомления и email-рассылка (SMTP) по Kafka-событиям, шаблоны, настройки каналов. |
| **reports-service** | 8088 | `reports` | Аналитика и отчётность (CQRS read-модели): 5 отчётов + экспорт в CSV / XLSX / PDF. |
| **frontend** | 3000 | — | React SPA (админ / жилец / исполнитель / управляющий). |

У каждого сервиса есть собственный подробный `README.md` (или `HELP.md`) —
с полным списком эндпоинтов, событий и примерами `curl`.

---

## Технологический стек

**Backend (общий для сервисов):**
- Java 21, Spring Boot 3.3.5, Spring Cloud 2023.0.x, Maven
- Spring Web / WebFlux (Gateway), Spring Security (кастомный JWT-фильтр, HS256, `jjwt 0.12.x`)
- Spring Data JPA + Hibernate 6, PostgreSQL 17, **Liquibase** (по схеме на сервис)
- Spring Data Redis (refresh-токены, rate limiting, кеш межсервисных вызовов)
- Spring Kafka + **Transactional Outbox**
- Spring Cloud OpenFeign + **Resilience4j** (Circuit Breaker, fallback)
- MapStruct, Lombok
- springdoc-openapi 2.6 (Swagger UI)
- Micrometer Tracing + OpenTelemetry OTLP, Prometheus, Logstash JSON (Loki-ready)
- JUnit 5 + Mockito + AssertJ, Testcontainers, JaCoCo (≥ 70 % на `service.Impl`)
- reports-service дополнительно: Apache POI (XLSX), OpenCSV (CSV), openhtmltopdf + Thymeleaf (PDF)

**Frontend:**
- React 18 + TypeScript 5, Vite 5
- Ant Design 5, React Router 6
- TanStack Query 5 (server state), Zustand 5 (auth state)
- axios (interceptor с single-flight refresh JWT), react-hook-form + zod, dayjs
- Vitest + Testing Library

---

## Инфраструктура и порты

Из `docker/docker-compose.yml`:

| Компонент | Порт(ы) | Назначение |
|---|---|---|
| PostgreSQL 17 | 5432 | Единая БД `dormitory` (схема на сервис) |
| Redis 7 | 6379 | Refresh-токены, rate limiting, кеш |
| Kafka (KRaft) | 9092 | Шина доменных событий |
| MinIO | 9000 (API), 9001 (Console) | Хранилище вложений заявок |
| MailHog | 1025 (SMTP), 8025 (UI) | Перехват исходящих писем в dev |

Общие переменные окружения сервисов: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`,
`REDIS_HOST`/`REDIS_PORT`, `KAFKA_BROKERS`, `JWT_SECRET` (общий для всех!),
`SERVICE_TOKEN` (общий секрет для S2S), `SPRING_PROFILES_ACTIVE`
(`local` / `docker`). Полные таблицы — в README конкретного сервиса.

> ⚠️ Значения секретов в compose (`JWT_SECRET`, `SERVICE_TOKEN`,
> `ADMIN_INITIAL_PASSWORD`, доступы MinIO) — dev-дефолты. **Перед проливом в
> прод их обязательно нужно заменить.**

---

## Модель данных

Единая БД `dormitory`, изоляция по схемам (создаются в
`docker/init-db/01-create-schemas.sql`):

```
auth · requests · residents · appliances · consumables · notifications · reports
```

Каждый сервис ведёт миграции своей схемы через Liquibase. Схема `reports`
содержит денормализованные read-модели, которые строятся из Kafka-событий
(late JOIN для дозаполнения имён).

---

## Обмен событиями (Kafka)

Все события заворачиваются в единый envelope:

```json
{
  "eventId": "uuid",
  "eventType": "RequestCreated",
  "aggregateType": "request",
  "aggregateId": "uuid",
  "occurredAt": "2026-04-18T12:00:00Z",
  "payload": { }
}
```

| Топик | Продюсер | Ключевые события |
|---|---|---|
| `user.events` | auth-service | `USER_CREATED`, `USER_DEACTIVATED`, `PASSWORD_RESET_REQUESTED` |
| `resident.events` | residents-service | `ResidentEnrolled`, `ResidentEvicted`, `ResidentMoved` |
| `request.events` | requests-service | `RequestCreated`, `RequestAssigned`, `RequestStatusChanged`, `RequestClosed` |
| `appliance.events` | appliances-service | `ApplianceRegistered`, `ApplianceApproved`, `ApplianceRejected`, `ApplianceRevoked` |
| `consumable.events` | consumables-service | `ConsumableIssued`, `ConsumableReturned`, `ConsumableStockLow`, `ConsumableStockOut` |
| `notification.events` | consumables-service | `ConsumableReturnRequired` |

**Потоки-реакции:**
- `notifications-service` слушает `user/resident/request.events` → in-app + email.
- `appliances-service` и `consumables-service` слушают `resident.events`
  (`ResidentEvicted` → авто-revoke приборов / запрос возврата расходников).
- `reports-service` слушает все 5 топиков и строит read-модели; при ошибке —
  retry → DLT (`reports.dlt`).

---

## Аутентификация и роли

- **access token** (JWT, HS256, TTL 15 мин) — хранится в памяти клиента.
- **refresh token** (TTL 7 дней) — в `localStorage` фронта, в Redis на бэке;
  ротация с **reuse detection** (повторное использование инвалидирует всю
  «семью» токенов → 401).
- Gateway валидирует подпись/срок и пробрасывает в downstream заголовки
  `X-User-Id`, `X-User-Email`, `X-User-Roles`, `X-Request-Id`.

**8 ролей RBAC:** `ADMIN`, `RESIDENT`, `GATEKEEPER`, `EXECUTOR_ELECTRIC`,
`EXECUTOR_PLUMBING`, `EXECUTOR_CARPENTRY`, `EXECUTOR_GAS`, `PROPERTY_MANAGER`.
У пользователя может быть несколько ролей; в JWT кодируются как `ROLE_<CODE>`.

Заявки (`requests-service`) имеют статусную машину
`NEW → ASSIGNED → IN_PROGRESS → IN_REVIEW → DONE → CLOSED`
(+ `REJECTED`, `CANCELLED`) и маршрутизируются по пулам исполнителей
(`TargetPool`: сантехники, электрики, столяры, газовики, управляющий, админ).

---

## Быстрый старт

Требуется: **Docker Desktop** (Compose v2), для локальной разработки —
**JDK 21** и **Maven 3.9+** (или `mvnw`), **Node.js 20+**.

### 1. Всё в Docker Compose

Сервисы собираются из исходников (`build.context` в compose). Перед первым
запуском соберите jar'ы (Dockerfile'ы ожидают собранный артефакт — см. README
сервисов) либо используйте multistage-сборку, если она предусмотрена
Dockerfile'ом.

```bash
cd docker
docker compose up -d --build
```

Поднимутся инфраструктура и все микросервисы. Точка входа —
**http://localhost:8080**. Агрегированный Swagger —
**http://localhost:8080/swagger-ui.html**.

Frontend в compose закомментирован — раскомментируйте блок `frontend:` в
`docker/docker-compose.yml`, чтобы поднять SPA на `:3000`.

### 2. Гибридный режим (инфраструктура в Docker, сервис локально)

```bash
# только инфраструктура
cd docker
docker compose up -d postgres redis kafka minio mailhog

# нужный сервис — локально
cd ../services/auth-service
./mvnw spring-boot:run
```

### 3. Frontend (dev)

```bash
cd frontend
npm install
npm run dev            # http://localhost:5173
```

`.env.development` задаёт `VITE_API_BASE_URL` (по умолчанию
`http://localhost:8080`). Финальный baseURL axios — `${VITE_API_BASE_URL}/api/v1`.

### Проверка

```bash
# логин админом
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@dormitory.local","password":"Admin12345!"}'
```

Первый администратор создаётся автоматически (`AdminBootstrap`):
`admin@dormitory.local` / `${ADMIN_INITIAL_PASSWORD:Admin12345!}`.

---

## Демо-данные (seed)

`seed.sql` наполняет БД демонстрационными данными: ~15 пользователей со всеми
ролями (комендант, сантехник, электрик, столяр, вахтёр, студенты), комнаты,
заселения, заявки и т.д. Пароль всех сидированных пользователей —
`Admin12345!`.

```bash
docker exec -i dormitory-postgres psql -U postgres -d dormitory < seed.sql
```

Применять **после** старта сервисов (Liquibase должен создать таблицы).

---

## Наблюдаемость

- **Health / probes:** `GET /actuator/health` (liveness/readiness) у каждого
  сервиса; в compose healthcheck'и завязаны на `/actuator/health/readiness`.
- **Метрики:** `GET /actuator/prometheus` (Micrometer). У части сервисов есть
  кастомные метрики (напр. `notifications_emails_sent_total`,
  `notifications_events_processed_total`).
- **Трейсинг:** Micrometer Tracing + OpenTelemetry OTLP (`OTLP_ENDPOINT`,
  по умолчанию `http://localhost:4318/v1/traces`).
- **Логи:** формат с `[traceId,spanId]`; JSON-энкодер для Loki присутствует
  (в части сервисов закомментирован — включается вместе со стеком Loki/Grafana).

---

## Тесты

Каждый сервис:

```bash
cd services/<service>
./mvnw clean verify      # unit + JaCoCo (порог ≥ 70 % на service.Impl)
```

Интеграционные тесты (`*IT`) используют **Testcontainers** (PostgreSQL / Redis /
Kafka), GreenMail (SMTP), WireMock — требуют запущенного Docker. В части
сервисов включаются флагом:

```bash
./mvnw verify -DrunIntegrationTests=true
```

Frontend:

```bash
cd frontend
npm test                 # vitest
```

---

## Структура репозитория

```
dev/
├── docker/
│   ├── docker-compose.yml         # вся система: инфраструктура + сервисы
│   └── init-db/
│       └── 01-create-schemas.sql  # создание схем в БД dormitory
├── services/
│   ├── api-gateway/               # Spring Cloud Gateway (WebFlux)
│   ├── auth-service/              # аутентификация, пользователи, RBAC
│   ├── requests-service/          # заявки на обслуживание (+ MinIO вложения)
│   ├── residents-service/         # жильцы, комнаты, инвентарь
│   ├── appliances-service/        # личные электроприборы
│   ├── consumables-service/       # расходные материалы
│   ├── notifications-service/     # уведомления (in-app + email)
│   └── reports-service/           # отчётность (CSV/XLSX/PDF)
├── frontend/                      # React 18 + TS + Vite SPA
└── seed.sql                       # демо-данные
```

Каждая директория сервиса — самостоятельный Maven-модуль (`pom.xml`, `mvnw`,
`Dockerfile`) со своим `README.md`, где описаны все эндпоинты, события,
переменные окружения и примеры `curl`.
