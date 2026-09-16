import type { NotificationType } from '@/features/notifications/types';

export const NOTIFICATION_TYPE_LABELS: Record<NotificationType, string> = {
  WELCOME: 'Добро пожаловать',
  ACCOUNT_DEACTIVATED: 'Аккаунт деактивирован',
  PASSWORD_RESET: 'Сброс пароля',
  RESIDENT_ENROLLED: 'Заселение',
  RESIDENT_EVICTED: 'Выселение',
  RESIDENT_MOVED: 'Переселение',
  REQUEST_CREATED: 'Заявка создана',
  REQUEST_ASSIGNED: 'Заявка назначена',
  REQUEST_STATUS_CHANGED: 'Статус заявки изменён',
  REQUEST_CLOSED: 'Заявка закрыта',
  SYSTEM: 'Система',
};

export const NOTIFICATION_TYPE_OPTIONS = (
  Object.keys(NOTIFICATION_TYPE_LABELS) as NotificationType[]
).map((value) => ({ value, label: NOTIFICATION_TYPE_LABELS[value] }));
