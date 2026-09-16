# api-gateway

Реактивный API Gateway (Spring Cloud Gateway, WebFlux) — единая точка входа для веб-клиента.

## Что делает

- Маршрутизирует входящие запросы к downstream-сервисам по prefix path.
- Валидирует JWT (подпись и срок действия), пробрасывает `X-User-Id`, `X-User-Email`, `X-User-Roles` в downstream.
- Гарантирует наличие `X-Request-Id` (генерирует, если нет).
- Применяет CORS и rate limiting (Redis-based, 30 req/s, burst 60).
- Возвращает ошибки в едином формате `ErrorResponseDTO`.
- Объединяет Swagger всех downstream-сервисов на `/swagger-ui.html`.

## Стек

Java 21, Spring Boot 3.3, Spring Cloud Gateway 2023.0.x (WebFlux), JJWT 0.12, Reactive Redis, Micrometer + OTLP, Springdoc OpenAPI.

## Порты и зависимости

- Слушает: **8080**.
- Зависимости: Redis (rate limiting), downstream-сервисы (auth, residents, requests, appliances, consumables, notifications, reports).

## Конфигурация (env)

| Переменная | Назначение | Дефолт |
|---|---|---|
| `JWT_SECRET` | HMAC-секрет (≥ 32 символа), тот же, что в auth-service | dev-секрет |
| `REDIS_HOST` / `REDIS_PORT` | Redis | localhost / 6379 |
| `CORS_ORIGINS` | Список разрешённых origin через запятую | `http://localhost:3000,http://localhost:5173` |
| `RATE_LIMIT_REPLENISH` | Запросов в секунду | 30 |
| `RATE_LIMIT_BURST` | Пиковая ёмкость | 60 |
| `AUTH_SERVICE_URL` ... `REPORTS_SERVICE_URL` | URL downstream-сервисов | `http://localhost:808X` |
| `OTLP_ENDPOINT` | OpenTelemetry collector | `http://localhost:4318/v1/traces` |
| `SPRING_PROFILES_ACTIVE` | Профиль (`local` / `docker`) | `local` |

## Запуск локально

```bash
# 1. Поднять Redis
docker run -d -p 6379:6379 redis:7-alpine

# 2. Запустить downstream-сервисы (auth-service, residents-service и т.д.) на их портах

# 3. Сборка и запуск gateway
mvn clean package
java -jar target/api-gateway.jar
```

## Запуск в docker-compose

Из корня `dev/`:

```bash
docker compose -f docker/docker-compose.yml up --build api-gateway
```

## Маршруты

| Path prefix | Downstream |
|---|---|
| `/api/v1/auth/**`, `/api/v1/users/**` | auth-service |
| `/api/v1/residents/**`, `/api/v1/rooms/**`, `/api/v1/inventory/**`, `/api/v1/buildings/**`, `/api/v1/floors/**` | residents-service |
| `/api/v1/requests/**` | requests-service |
| `/api/v1/appliances/**` | appliances-service |
| `/api/v1/consumables/**` | consumables-service |
| `/api/v1/notifications/**` | notifications-service |
| `/api/v1/reports/**` | reports-service |
| `/swagger-ui.html`, `/v3/api-docs/**` | Aggregated Swagger |
| `/actuator/**` | Health, metrics, gateway info |

## Whitelist (без JWT)

`/api/v1/auth/login`, `/api/v1/auth/register`, `/api/v1/auth/refresh`, `/api/v1/auth/password/reset-request`, `/api/v1/auth/password/reset`, `/actuator/**`, `/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html`, `/webjars/**`.

## Примеры curl

### Логин (без JWT)

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@dormitory.local","password":"Admin12345!"}'
```

### Запрос с JWT

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@dormitory.local","password":"Admin12345!"}' | jq -r .accessToken)

curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8080/api/v1/residents
```

### Запрос без JWT к защищённому endpoint → 401

```bash
curl -i http://localhost:8080/api/v1/requests
# HTTP/1.1 401 Unauthorized
# {"timestamp":"...","error":"Unauthorized","message":"Отсутствует заголовок Authorization", ...}
```

### CORS preflight

```bash
curl -i -X OPTIONS http://localhost:8080/api/v1/requests \
  -H 'Origin: http://localhost:5173' \
  -H 'Access-Control-Request-Method: GET'
```

### Rate limit (31+ запрос за секунду от одного юзера → 429)

```bash
for i in {1..40}; do
  curl -s -o /dev/null -w "%{http_code}\n" \
       -H "Authorization: Bearer $TOKEN" \
       http://localhost:8080/api/v1/requests
done
# Часть запросов вернёт 429 с заголовком Retry-After.
```

## Метрики и логи

- Prometheus: `GET /actuator/prometheus` — стандартные метрики gateway.
- Health: `GET /actuator/health` (probes: liveness/readiness).
- Gateway routes: `GET /actuator/gateway/routes` — список активных маршрутов.
- Логи: формат `%d %level [traceId,spanId] logger - msg`. В docker — JSON через Loki4jAppender (закомментирован, включить вместе с Loki).

## Архитектура фильтров (порядок)

1. `TraceIdFilter` (HIGHEST_PRECEDENCE) — гарантирует X-Request-Id.
2. `JwtAuthGlobalFilter` (+100) — валидация JWT, пробрасывание X-User-* заголовков.
3. `RequestLoggingFilter` (+200) — логирование входа/выхода с длительностью.
4. `RequestRateLimiter` (default-filter) — Redis token bucket по ключу `user:<id>` или `ip:<addr>`.
5. CORS (`CorsWebFilter`) — глобальный bean.
6. `GlobalExceptionHandler` (`ErrorWebExceptionHandler`, Order=-2) — единый формат ошибок.

## Ограничения

- Eureka не используется: маршруты на прямые URI (`http://service-name:port`). Это решение продиктовано отсутствием Eureka в `docker-compose.yml`.
- HTTPS-терминирование — на reverse proxy (nginx) перед Gateway.
- Валидация ролей — в downstream через `@PreAuthorize`. Gateway проверяет только подпись и срок токена.

## Тесты

```bash
mvn test
```

- `JwtValidatorTest` — валидный, невалидный, истёкший, мусорный токен.
- `KeyResolverTest` — userId vs IP fallback.
