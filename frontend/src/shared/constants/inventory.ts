import type {
  InventoryState,
  InventoryType,
} from '@/features/inventory/types';

export const INVENTORY_TYPE_LABELS: Record<InventoryType, string> = {
  BED: 'Кровать',
  DESK: 'Стол',
  CHAIR: 'Стул',
  CABINET: 'Шкаф',
  NIGHTSTAND: 'Тумбочка',
  REFRIGERATOR: 'Холодильник',
  STOVE: 'Плита',
  WASHING_MACHINE: 'Стиральная машина',
  KETTLE: 'Чайник',
  OTHER: 'Другое',
};

export const INVENTORY_STATE_LABELS: Record<InventoryState, string> = {
  NEW: 'Новое',
  USED: 'Б/у',
  BROKEN: 'Сломано',
  WRITTEN_OFF: 'Списано',
};

export const INVENTORY_STATE_COLORS: Record<InventoryState, string> = {
  NEW: 'green',
  USED: 'blue',
  BROKEN: 'orange',
  WRITTEN_OFF: 'default',
};

export const INVENTORY_TYPE_OPTIONS = (
  Object.keys(INVENTORY_TYPE_LABELS) as InventoryType[]
).map((value) => ({ value, label: INVENTORY_TYPE_LABELS[value] }));

export const INVENTORY_STATE_OPTIONS = (
  Object.keys(INVENTORY_STATE_LABELS) as InventoryState[]
).map((value) => ({ value, label: INVENTORY_STATE_LABELS[value] }));
