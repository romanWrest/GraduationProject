import type { RequestStatus, RequestType, TargetPool } from '@/features/requests/types';

export const REQUEST_TYPE_LABELS: Record<RequestType, string> = {
  REPAIR_ELECTRIC: 'Электрика',
  REPAIR_PLUMBING: 'Сантехника',
  REPAIR_CARPENTRY: 'Плотницкие работы',
  REPAIR_GAS: 'Газ',
  LINEN_REPLACEMENT: 'Замена белья',
  GUEST_PASS: 'Пропуск гостя',
  ITEM_MOVEMENT: 'Перемещение мебели',
  RELOCATION: 'Переселение',
  COMPLAINT: 'Жалоба',
  ELECTRICAL_APPLIANCE: 'Электроприбор',
  OTHER: 'Другое',
};

export const REQUEST_STATUS_LABELS: Record<RequestStatus, string> = {
  NEW: 'Новая',
  IN_REVIEW: 'На рассмотрении',
  ASSIGNED: 'Назначена',
  IN_PROGRESS: 'В работе',
  DONE: 'Выполнена',
  CLOSED: 'Закрыта',
  REJECTED: 'Отклонена',
  CANCELLED: 'Отменена',
};

export const REQUEST_TYPE_OPTIONS = (Object.keys(REQUEST_TYPE_LABELS) as RequestType[]).map(
  (value) => ({ value, label: REQUEST_TYPE_LABELS[value] }),
);

export const REQUEST_STATUS_OPTIONS = (Object.keys(REQUEST_STATUS_LABELS) as RequestStatus[]).map(
  (value) => ({ value, label: REQUEST_STATUS_LABELS[value] }),
);

export const TARGET_POOL_LABELS: Record<TargetPool, string> = {
  EXECUTOR_ELECTRIC: 'Электрики',
  EXECUTOR_PLUMBING: 'Сантехники',
  EXECUTOR_CARPENTRY: 'Плотники',
  EXECUTOR_GAS: 'Газовики',
  PROPERTY_MANAGER: 'Управляющий имуществом',
  ADMIN: 'Администрация',
};
