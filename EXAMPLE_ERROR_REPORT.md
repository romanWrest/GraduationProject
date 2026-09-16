# Отчёт о тестировании СУО (dormitory)

- Дата: 2026-05-14
- Окружение: api-gateway:8080, auth:8081, requests:8082, residents:8083, appliances:8084, consumables:8085, **notifications:8087 (не запущен)**, reports:8088, frontend:5173
- Docker‑инфра: dormitory-postgres / dormitory-redis / dormitory-kafka / dormitory-minio / dormitory-mailhog — все Up

## Что засеяно

Файл `seed.sql` применён к `dormitory`. Сводка строк:

| Таблица | Строк |
|---|---|
| auth.users | 15 |
| auth.user_roles | 16 |
| residents.room | 12 |
| residents.resident | 12 |
| residents.residency_history | 13 |
| residents.inventory_item | 15 |
| requests.request | 12 |
| requests.request_status_history | 10 |
| requests.request_comment | 10 |
| requests.request_attachment | 5 |
| appliances.appliance | 12 |
| appliances.appliance_history | 10 |
| consumables.consumable_type | 4 (из init) |
| consumables.consumable_issue | 12 |
| consumables.stock_movement | 12 |
| reports.users_view | 15 |
| reports.residents_view | 12 |
| reports.requests_view | 12 |
| reports.appliances_view | 12 |
| reports.consumables_view | 12 |
| notifications.* | **0 (схема пустая, миграции не применены)** |

Логика данных: пользователи → роли → жильцы → комнаты → инвентарь → заявки (со статусами/историей/комментариями/вложениями) → электроприборы (с историей решений) → выдачи расходников + движения склада → снимки в `reports.*` (для оффлайн отчётов). Все UUID детерминированные (`aXXXX…` для users, `bXXXX…` для rooms, `cXXXX…` для residents и т. д.) — удобно перекрёстно ссылаться.

Пароль всех новых пользователей: `Admin12345!` (использован существующий bcrypt хеш админа).

В ходе посева пришлось править значения, чтобы они проходили валидацию enum'ов в сервисах (см. п. «Несоответствие enum'ам» ниже).

---

## Найденные ошибки

### 1. КРИТИЧНО — `ERROR: function lower(bytea) does not exist` на листингах с опциональными фильтрами

Проявляется на нескольких сервисах при запросе без всех опциональных параметров (или с null-параметром). Hibernate подставляет `?` без явного cast, и PostgreSQL для конструкции `lower(?)` выбирает тип `bytea` вместо `text`.

**Воспроизведение:**

| Эндпоинт | Статус |
|---|---|
| `GET /api/v1/users` (auth-service) | 500 |
| `GET /api/v1/residents` (residents-service) | 500 |
| `GET /api/v1/appliances` (appliances-service) | 500 |
| `GET /api/v1/consumables/issues` (consumables-service) | 500 |

Пример SQL из стек-трейса:
```
... where (? is null or r1_0.kind=?) ...
        or lower(coalesce(r1_0.faculty,'')) like lower(('%'||?||'%')) escape '' ...
ERROR: function lower(bytea) does not exist
Hint: No function matches the given name and argument types.
```

**Файлы, которые с очень большой вероятностью являются источником:** Spring Data JPA Specifications или JPQL с `(:p is null OR ... like ('%'||:p||'%'))` в репозиториях residents/appliances/consumables/auth.

**Как чинить (один из вариантов):**
- В JPQL/Criteria явно типизировать параметр: `cast(:p as string)` / `criteriaBuilder.literal((String) null)` вместо безымянного позиционного `?`.
- Или собирать предикаты only-when-not-null в Specification вместо `(? is null OR ...)`.
- Или в `application.yml`: `spring.jpa.properties.hibernate.query.in_clause_parameter_padding=false` + `hibernate.jdbc.fetch_size=...` не помогут — нужна правка SQL.

Эта ошибка ломает основные административные экраны (списки жильцов / приборов / расходников / пользователей) — фронт будет получать 500.

---

### 2. КРИТИЧНО — `LazyInitializationException: could not initialize proxy [Room] – no Session`

Проявляется во всех эндпоинтах residents-service, где DTO собирается из сущности `Resident`/`Room` после закрытия транзакции (OSIV выключен, а маппер обращается к lazy полю).

| Эндпоинт | Статус |
|---|---|
| `GET /api/v1/rooms/{id}` | 500 |
| `GET /api/v1/residents/{id}` | 500 |
| `GET /api/v1/rooms/{id}/residents` | 500 |
| `GET /api/v1/residents/by-user/{userId}` | 500 |

Текст: `could not initialize proxy [ru.dstu.dormitory.residents_service.domain.model.Room#…] - no Session`.

**Как чинить:**
- В сервисном методе явно `JOIN FETCH` Room через `@EntityGraph` или JPQL `select r from Resident r join fetch r.room where r.id = :id`.
- Либо `@Transactional(readOnly = true)` обернуть весь pipeline маппинга (включая mapper).
- Либо включить OSIV `spring.jpa.open-in-view: true` (быстро, но плохая практика).

`GET /api/v1/rooms` (листинг) работает — там, видимо, только агрегаты без обращения к lazy полям.

---

### 3. БЛОКЕР — `notifications-service` не запущен

- Порт 8087 не слушается. Через gateway: `GET /api/v1/notifications` → `503 Service Unavailable`, "Не удалось подключиться к downstream-сервису".
- Схема `notifications` в БД присутствует (создаётся init-скриптом), **но таблиц в ней нет** — Liquibase сервиса не запускался.
- В docker-compose сервис закомментирован — поднимается локально вне docker, но сейчас не запущен.

Нужно поднять `notifications-service` (или раскомментировать в compose), после чего применятся миграции.

---

### 4. Маршрутизация: путь `/api/v1/residents/rooms` отсутствует

Возможная мина для фронта/документации: интуитивно ожидается, что комнаты живут под префиксом residents-service, но реальный путь — `/api/v1/rooms` (через gateway тоже). Запрос на `/api/v1/residents/rooms` вернёт `400` (gateway маршрутизирует под `/api/v1/residents/{id}`, и слово `rooms` парсится как UUID).

Рекомендация: либо документировать явно, либо завести алиас `/api/v1/residents/rooms` в gateway/residents-service.

---

### 5. Обязательные параметры дат в reports без дефолта

`GET /api/v1/reports/requests` и `GET /api/v1/reports/consumables` без `from`/`to` отвечают 400. При этом `GET /api/v1/reports/residents` и `GET /api/v1/reports/appliances` работают без параметров. Поведение несимметричное.

Решение: сделать `from`/`to` опциональными со значениями по умолчанию (например, последние 30 дней).

---

### 6. Несоответствие enum'ам между сервисами и доменом

При попытке загрузить «логичные» значения через SQL получены 500 — пришлось править. Это в первую очередь намекает на то, что нет общего справочника enum или фронт может промахнуться:

| Сервис / поле | Реальный enum | Что часто пишут |
|---|---|---|
| `requests.request.type` | `REPAIR_ELECTRIC, REPAIR_PLUMBING, REPAIR_CARPENTRY, REPAIR_GAS, LINEN_REPLACEMENT, GUEST_PASS, ITEM_MOVEMENT, RELOCATION, COMPLAINT, ELECTRICAL_APPLIANCE, OTHER` | `PLUMBING`, `ELECTRIC`, `CARPENTRY`, `GAS`, `CLEANING` — все вне enum |
| `appliances.appliance.type` | `KETTLE, MICROWAVE, FRIDGE, LAPTOP_CHARGER, PHONE_CHARGER, IRON, HAIR_DRYER, TOASTER, HEATER, FAN, LAMP, TV, SPEAKER, OTHER` | `WASHING_MACHINE`, `COFFEE_MAKER` — вне enum |
| `consumables.consumable_issue.status` | `ISSUED, RETURNED` | `LOST` — это **не статус**, а `returnCondition` |
| `consumables.consumable_issue.return_condition` | `OK, DAMAGED, LOST` | `GOOD`, `WORN` — вне enum |

Рекомендация: фронту брать enum'ы из `/v3/api-docs` и не зашивать своими словарями. В БД нет CHECK-констрейнтов на enum значения — поэтому SQL-вставка проходит, а GET через сервис падает с `IllegalArgumentException: No enum constant`. Стоит добавить CHECK constraints или enum типы в PostgreSQL.

---

### 7. `GET /api/v1/auth/me` отсутствует

В auth-service маршрут — `GET /api/v1/users/me` (200, отдает текущего пользователя). Если фронт пытается дергать `/api/v1/auth/me` (как часто пишут в туториалах), получит 500 с «No static resource api/v1/auth/me» (это сам по себе странный текст ошибки — Spring 6 отдаёт 404 как 500 из-за GlobalExceptionHandler).

Можно завести алиас или поправить обработку 404 в GlobalExceptionHandler.

---

## Эндпоинты, которые точно работают

| Эндпоинт | Статус |
|---|---|
| `POST /api/v1/auth/login` | 200 |
| `POST /api/v1/auth/login` (неверный пароль) | 401 с осмысленным сообщением |
| `POST /api/v1/auth/login` (битый JSON) | 400 с осмысленным сообщением |
| `GET /api/v1/users/me` | 200 |
| `GET /api/v1/rooms?size=N` | 200, корректный `occupied` по жильцам |
| `GET /api/v1/rooms/{id}/inventory` | 200 |
| `GET /api/v1/requests?size=N` | 200 |
| `GET /api/v1/requests/{id}` | 200 |
| `GET /api/v1/requests/{id}/comments` | 200 |
| `GET /api/v1/appliances/{id}` | 200 |
| `GET /api/v1/consumables/types` | 200 |
| `GET /api/v1/reports/residents` | 200 (агрегаты по факультетам/комнатам) |
| `GET /api/v1/reports/appliances` | 200 |
| `GET /api/v1/reports/requests?from=...&to=...` | 200 |
| `GET /api/v1/reports/consumables?from=...&to=...` | 200 |
| `GET /api/v1/reports/sync-status` | 200 (но `processed=0` по всем топикам — см. п. 8) |

---

### 8. Дополнительно: `reports.sync-status` показывает 0 обработанных событий

После сидинга прямые INSERT в `*.outbox_events` пустые → Kafka не получала событий → `reports`/`notifications` ничего не реплицировали. Для боевой системы это норм, но в тестовой среде это значит, что **`reports.*_view` теперь живёт в виде ручного снимка** (засеян прямо SQL), а не через шину. Если запустить сервисы и они начнут публиковать события (например, через UI), у reports появится дублирование (если есть `ON CONFLICT`) или конфликт PK (если нет).

Я в seed‑скрипте использовал `ON CONFLICT DO NOTHING/UPDATE` именно для этого — но в боевом режиме лучше перезаливать reports через Kafka, а не через прямой INSERT.

---

## Что ещё стоит починить (по моему мнению)

1. **Включить в docker-compose все сервисы** — половина закомментирована, и без `notifications-service` фронт не сможет показать колокольчик.
2. **Добавить CHECK-констрейнты на enum-поля** (или PostgreSQL `CREATE TYPE … AS ENUM`) — это поймает 80% «грязных» данных на уровне БД, а не при GET.
3. **GlobalExceptionHandler**: 404 на отсутствующий маршрут выдаёт 500 с текстом «No static resource …». Стоит маппить `NoResourceFoundException` → `404`.
4. **OpenAPI**: фронту нужно явное описание `Pageable` в query (сейчас в /v3/api-docs он отдаётся как объект, что в Swagger UI неудобно).

---

## Артефакты

- `seed.sql` — лежит в `C:\dev\seed.sql`, идемпотентен по `ON CONFLICT` для reports‑слоя, но не для остальных схем (повторный запуск упадёт на PK). Запускался как `docker cp seed.sql dormitory-postgres:/tmp/ && docker exec ... psql -f /tmp/seed.sql`.
- Этот отчёт — `C:\dev\ERROR_REPORT.md`.
