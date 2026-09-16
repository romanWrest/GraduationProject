import type { ApplianceStatus, ApplianceType } from '@/features/appliances/types';

export const APPLIANCE_TYPE_LABELS: Record<ApplianceType, string> = {
  KETTLE: 'Чайник',
  MICROWAVE: 'Микроволновка',
  FRIDGE: 'Холодильник',
  LAPTOP_CHARGER: 'Зарядка ноутбука',
  PHONE_CHARGER: 'Зарядка телефона',
  IRON: 'Утюг',
  HAIR_DRYER: 'Фен',
  TOASTER: 'Тостер',
  HEATER: 'Обогреватель',
  FAN: 'Вентилятор',
  LAMP: 'Лампа',
  TV: 'Телевизор',
  SPEAKER: 'Колонки',
  OTHER: 'Другое',
};

export const APPLIANCE_TYPE_OPTIONS = (
  Object.keys(APPLIANCE_TYPE_LABELS) as ApplianceType[]
).map((value) => ({ value, label: APPLIANCE_TYPE_LABELS[value] }));

export const APPLIANCE_STATUS_LABELS: Record<ApplianceStatus, string> = {
  PENDING: 'На рассмотрении',
  APPROVED: 'Одобрен',
  REJECTED: 'Отклонён',
  REVOKED: 'Снят',
};

export const APPLIANCE_STATUS_COLORS: Record<ApplianceStatus, string> = {
  PENDING: 'gold',
  APPROVED: 'green',
  REJECTED: 'red',
  REVOKED: 'default',
};

export const APPLIANCE_STATUS_OPTIONS = (
  Object.keys(APPLIANCE_STATUS_LABELS) as ApplianceStatus[]
).map((value) => ({ value, label: APPLIANCE_STATUS_LABELS[value] }));
