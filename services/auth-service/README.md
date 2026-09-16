# auth-service

Микросервис аутентификации, авторизации и управления учётными записями для системы
управления общежитием (диплом).

Отвечает за регистрацию/создание пользователей, выдачу и ротацию JWT (access + refresh),
сброс паролей, RBAC, аудит и публикацию событий о пользователях в Kafka.

## Стек

- Java 21, Spring Boot 3.3.5, Spring Cloud 2023.0.3, Maven
- Spring Security (кастомный JWT filter, без OAuth2 resource server) — HS256, `jjwt 0.12.x`
- Spring Data JPA + Hibernate 6, PostgreSQL 17, Liquibase (schema `auth`)
- Spring Data Redis (StringRedisTemplate) — refresh-токены и rate limiting
- Spring Kafka + Outbox pattern (транзакционный outbox → scheduled relay)
- springdoc-openapi 2.6 — Swagger UI
- Micrometer Tracing + OpenTelemetry OTLP (без Sleuth), Prometheus, Logstash JSON
- JUnit 5 + Mockito + AssertJ, JaCoCo ≥ 70% на `service.Impl`

## Эндпоинты

### Публичные (`/api/v1/auth/**`)

| Метод | Путь                                  | Описание                            |
|-------|---------------------------------------|-------------------------------------|
| POST  | `/api/v1/auth/login`                  | Логин по email+паролю               |
| POST  | `/api/v1/auth/refresh`                | Ротация пары access + refresh       |
| POST  | `/api/v1/auth/password-reset/request` | Запрос на сброс пароля              |
| POST  | `/api/v1/auth/password-reset/confirm` | Применение сброса по токену         |

### Защищённые (Bearer JWT)

| Метод | Путь                             | Роль                | Описание                        |
|-------|----------------------------------|---------------------|---------------------------------|
| POST  | `/api/v1/auth/logout`            | any authenticated   | Отзыв refresh-токена            |
| POST  | `/api/v1/auth/password-change`   | any authenticated   | Смена собственного пароля       |
| GET   | `/api/v1/users/me`               | any authenticated   | Профиль текущего пользователя   |
| PUT   | `/api/v1/users/me`               | any authenticated   | Обновление собственного профиля |
| GET   | `/api/v1/users`                  | `ADMIN`             | Поиск пользователей             |
| GET   | `/api/v1/users/{id}`             | `ADMIN`             | Пользователь по id              |
| POST  | `/api/v1/users`                  | `ADMIN`             | Создание пользователя           |
| PUT   | `/api/v1/users/{id}`             | `ADMIN`             | Обновление пользователя         |
| PUT   | `/api/v1/users/{id}/status`      | `ADMIN`             | active: true/false              |
| PUT   | `/api/v1/users/{id}/roles`       | `ADMIN`             | Смена набора ролей              |
| DELETE| `/api/v1/users/{id}`             | `ADMIN`             | Деактивация                     |

### Служебные (`/api/v1/internal/**`) — `X-Service-Token` или `ADMIN`

| Метод | Путь                                   | Описание                    |
|-------|----------------------------------------|-----------------------------|
| GET   | `/api/v1/internal/users/{id}`          | Профиль по id (S2S)         |
| POST  | `/api/v1/internal/users/batch`         | Batch-получение по списку id|

### Роли (RBAC, `auth.roles.code`)

`ADMIN`, `RESIDENT`, `GATEKEEPER`, `EXECUTOR_ELECTRIC`, `EXECUTOR_PLUMBING`,
`EXECUTOR_CARPENTRY`, `EXECUTOR_GAS`, `PROPERTY_MANAGER`.

Любая роль в JWT кодируется как `ROLE_<CODE>`.

## Kafka-события (outbox → topic `user.events`)

Формат envelope:

```json
{
  "eventId": "uuid",
  "eventType": "USER_CREATED | USER_DEACTIVATED | PASSWORD_RESET_REQUESTED",
  "aggregateType": "USER",
  "aggregateId": "uuid",
  "occurredAt": "2026-04-18T12:00:00Z",
  "payload": { ... }
}
```

- `USER_CREATED` — payload содержит временный пароль (`temporaryPassword`)
- `USER_DEACTIVATED` — при выставлении `active=false`
- `PASSWORD_RESET_REQUESTED` — содержит одноразовый raw-токен сброса

## Refresh-токены в Redis

Схема ключей (TTL = `jwt.refresh-ttl`):

| Ключ                                   | Назначение                                            |
|----------------------------------------|-------------------------------------------------------|
| `auth:refresh:<hash>`                  | Активный токен, значение `userId\|familyId`          |
| `auth:refresh:family:<familyId>`       | Set хешей токенов семьи (для массового отзыва)        |
| `auth:refresh:used:<hash>`             | Ранее использованный токен → триггер reuse detection  |
| `auth:refresh:user:<userId>`           | Set familyId пользователя                             |

При повторном использовании (reuse) `rotate` инвалидирует всю семью и бросает
`RefreshTokenReuseException` → 401.

## Запуск локально

Требуется: JDK 21, Maven 3.9+, Docker (для PostgreSQL, Redis, Kafka).

```bash
# инфраструктура (из корня репо)
docker compose -f docker/docker-compose.auth.yml up -d postgres redis kafka

# собрать и запустить
cd services/auth-service
mvn -DskipTests package
java -jar target/auth-service.jar
```

Swagger UI: http://localhost:8081/swagger-ui.html
Actuator:   http://localhost:8081/actuator/health
Prometheus: http://localhost:8081/actuator/prometheus

Первый админ создаётся автоматически (`AdminBootstrap`):

- email: `admin@dormitory.local`
- пароль: `${ADMIN_INITIAL_PASSWORD:Admin12345!}`

## Docker

```bash
cd services/auth-service && mvn -DskipTests package
cd ../../docker && docker compose -f docker-compose.auth.yml up --build
```

## Тесты и покрытие

```bash
mvn verify
```

JaCoCo-отчёт: `target/site/jacoco/index.html`. Правило: ≥70% инструкций в пакете
`ru.dstu.dormitory.auth_service.service.Impl`. 47 unit-тестов (service + util + aspect).

Интеграционные тесты (`*IT`) используют Testcontainers (PostgreSQL + Redis + Kafka)
и требуют запущенный Docker. По умолчанию отключены. Запуск:

```bash
mvn verify -DrunIntegrationTests=true
```

## Примеры curl

Логин:
```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@dormitory.local","password":"Admin12345!"}'
```

Ответ:
```json
{"accessToken":"eyJ...","refreshToken":"...uuid...","expiresIn":900}
```

Ротация:
```bash
curl -X POST http://localhost:8081/api/v1/auth/refresh \
  -H 'Content-Type: application/json' \
  -d '{"refreshToken":"<previous refresh>"}'
```

Создание пользователя (ADMIN):
```bash
curl -X POST http://localhost:8081/api/v1/users \
  -H "Authorization: Bearer $ACCESS" \
  -H 'Content-Type: application/json' \
  -d '{"email":"user@example.com","fullName":"Иван Иванов","roles":["RESIDENT"]}'
```

Смена пароля:
```bash
curl -X POST http://localhost:8081/api/v1/auth/password-change \
  -H "Authorization: Bearer $ACCESS" \
  -H 'Content-Type: application/json' \
  -d '{"oldPassword":"Admin12345!","newPassword":"NewPass12345"}'
```

## Конвенции

- Все бизнес-методы сервисного слоя помечены `@LogMethod(maskArgs={"password","token",...})`
- Все строки в `log.*(…)` — только `LogPatterns.*` константы
- `GlobalExceptionHandler` использует единый метод `setErrorResponseDTO(ex, description, status)`
  и возвращает `ErrorResponseDTO`
- Доменные исключения расширяют `RuntimeException`
- Сервисы = интерфейс + `Impl` класс, constructor injection (`@RequiredArgsConstructor`)
- Русскоязычные сообщения в исключениях и Javadoc

## Переменные окружения

| Переменная                | Назначение                        | Default                                              |
|---------------------------|-----------------------------------|------------------------------------------------------|
| `SPRING_PROFILES_ACTIVE`  | профиль                           | `local`                                              |
| `DB_URL`                  | JDBC URL                          | `jdbc:postgresql://localhost:5432/dormitory`         |
| `DB_USERNAME` / `DB_PASSWORD` | доступ к БД                   | `postgres` / `postgres`                              |
| `REDIS_HOST` / `REDIS_PORT`| адрес Redis                      | `localhost` / `6379`                                 |
| `KAFKA_BROKERS`           | bootstrap-servers                 | `localhost:9092`                                     |
| `JWT_SECRET`              | HS256 secret, ≥32 символа         | dev-дефолт (меняется в проде!)                       |
| `JWT_ACCESS_TTL`          | TTL access-токена (ISO-8601)      | `PT15M`                                              |
| `JWT_REFRESH_TTL`         | TTL refresh-токена                | `P7D`                                                |
| `SERVICE_TOKEN`           | header для служебных вызовов      | dev-дефолт                                           |
| `CORS_ORIGINS`            | список фронтендов через запятую   | `http://localhost:3000,http://localhost:5173`        |
| `ADMIN_INITIAL_PASSWORD`  | стартовый пароль админа           | `Admin12345!`                                        |
| `OTLP_ENDPOINT`           | OTLP traces endpoint              | `http://localhost:4318/v1/traces`                    |
