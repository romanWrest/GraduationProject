# Frontend — Система управления общежитием (СУО ДГТУ)

SPA для дипломного проекта. React 18 + TypeScript + Vite + Ant Design 5 + TanStack Query + Zustand.
Общается с backend через API Gateway (`/api/v1/...`).

## Стек

- **React 18** + **TypeScript 5** (функциональные компоненты, хуки)
- **Vite 5** — сборка и dev-server
- **Ant Design 5** + `@ant-design/icons` — UI
- **React Router 6** — роутинг с `createBrowserRouter`
- **TanStack Query 5** — server state, кэш, инвалидация, polling
- **Zustand 5** — client state (auth)
- **axios** — HTTP-клиент с interceptor'ами (auto refresh JWT)
- **react-hook-form** + **zod** — формы и валидация
- **dayjs** — даты (локаль `ru`)
- **vitest** + **@testing-library/react** — тесты

## Структура

```
src/
├── App.tsx                   # ConfigProvider AntD + QueryClient + RouterProvider
├── main.tsx
├── routes/                   # createBrowserRouter, ProtectedRoute, RoleRoute
├── pages/                    # экраны (admin/, resident/, executor/, property-manager/, requests/, notifications/)
├── features/                 # фича-модули: api, hooks, types, store
│   ├── auth/
│   ├── users/
│   ├── requests/
│   ├── residents/
│   ├── rooms/
│   ├── inventory/
│   ├── appliances/
│   ├── consumables/
│   ├── notifications/
│   └── reports/
├── components/
│   ├── layout/               # AppLayout, Sidebar (по ролям), HeaderBar (колокольчик с polling)
│   ├── common/               # PageHeader
│   └── reports/              # ExportButton, Aggregates
└── shared/
    ├── api/                  # axios client + interceptors (single-flight refresh)
    ├── lib/                  # roles, format, handleApiError
    ├── constants/            # roles, requestTypes, statusColors, ...
    └── hooks/                # useDebounce
```

## Запуск (dev)

Требуется поднятый backend (api-gateway на `:8080`).

```bash
npm install
npm run dev          # http://localhost:5173
```

`.env.development` настраивает `VITE_API_BASE_URL` (по умолчанию `http://localhost:8080`).
Финальный baseURL axios — `${VITE_API_BASE_URL}/api/v1`.

## Скрипты

```bash
npm run dev          # vite dev-server, hot reload
npm run build        # tsc + vite build → ./dist
npm run preview      # предпросмотр прод-сборки
npm run test         # vitest watch
npm run test:ui      # vitest UI
npm run lint         # eslint src
npm run format       # prettier --write src
```

## Аутентификация

- **access token** хранится в памяти Zustand (защита от XSS).
- **refresh token** хранится в `localStorage` (`auth.refreshToken`).
- Axios interceptor автоматически:
  - подставляет `Authorization: Bearer <access>` в каждый запрос;
  - на 401 запускает single-flight refresh через `POST /api/v1/auth/refresh`;
  - на неудачный refresh — `logout()` и редирект на `/login`.
- При старте `App` `AuthHydrator` восстанавливает сессию: если в `localStorage` есть refreshToken — делает refresh и подгружает `GET /users/me`.

## Роли

8 ролей RBAC: `ADMIN`, `RESIDENT`, `GATEKEEPER`, `EXECUTOR_ELECTRIC`,
`EXECUTOR_PLUMBING`, `EXECUTOR_CARPENTRY`, `EXECUTOR_GAS`, `PROPERTY_MANAGER`.
У одного пользователя может быть несколько ролей. `Sidebar` строит меню в
зависимости от ролей текущего пользователя; `RoleRoute` защищает страницы.

`gate-service` не реализован на backend — экраны вахтёра в MVP не делаем.

## Уведомления

`useUnreadCount` пульсирует `GET /api/v1/notifications/unread-count` каждые
30 секунд (`VITE_NOTIFICATIONS_POLL_INTERVAL_MS`). Колокольчик в `HeaderBar`
показывает счётчик. Полный список — на `/notifications`, настройки каналов
(email / in-app) — на `/notifications/settings`.

## Отчёты

`/reports` — хаб со ссылками на 5 отчётов (заявки, исполнители, проживающие,
приборы, расходники). Каждый отчёт — фильтры → агрегаты → таблица + кнопка
«Экспорт» (CSV / XLSX / PDF), которая скачивает файл напрямую через
`GET /api/v1/reports/{name}/export?format=...`.

## Контракты — расхождения с черновым ТЗ

Реальный backend местами отличается от первой версии ТЗ; типы и пути
выровнены под актуальный код:

- `POST /api/v1/auth/logout` принимает `{ refreshToken }`.
- Сброс пароля: `/auth/password-reset/request`, `/auth/password-reset/confirm`,
  смена — `/auth/password-change`.
- `/users/me`, `/users/{id}`, `/users/{id}/status`, `/users/{id}/roles` —
  `PUT` (не `PATCH`).
- Поиск пользователей — параметр `q` (не `search`).
- Создание пользователя возвращает `{ user, temporaryPassword }`.
- Заявки: `PATCH /requests/{id}` принимает `PatchActionDto`
  (`ASSIGN | START | DONE | CONFIRM | REJECT | REVIEW`); cancel и reopen —
  отдельные `POST`.
- Жильцы: PATCH'и через `/residents/{id}/evict`, `/move`; обновление полей —
  `PATCH /residents/{id}`.
- Инвентарь: типы, состояния, путь `/inventory/{id}` для CRUD.

## Тесты

```bash
npm test
```

Vitest покрывает критичную логику:
- `shared/lib/roles` — `hasRole`, `hasAnyRole`, `isExecutor`.
- `shared/lib/format` — даты, размеры, shortId.
- `features/auth/store` — токены и localStorage.

UI-снимки и презентационные компоненты намеренно не тестируем — фокус на
поведении, которое реально может сломаться.

## Production-сборка и Docker

```bash
npm run build
```

Сборка лежит в `./dist/`. Docker-образ — multistage (Node → Nginx).

```bash
docker build -t dormitory-frontend .
docker run --rm -p 3000:80 dormitory-frontend
```

`nginx.conf` отдаёт SPA с fallback на `index.html` и проксирует `/api/v1/`
на `http://api-gateway:8080` — внутри docker-compose сервис называется
`api-gateway`.

В `dev/docker/docker-compose.yml` есть закомментированный блок `frontend:` —
раскомментируйте, чтобы поднимать SPA в составе compose.
