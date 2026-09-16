import type {
  ConsumableUnit,
  IssueStatus,
  ReturnCondition,
} from '@/features/consumables/types';

export const UNIT_LABELS: Record<ConsumableUnit, string> = {
  PIECE: 'шт.',
  SET: 'компл.',
};

export const UNIT_OPTIONS = (Object.keys(UNIT_LABELS) as ConsumableUnit[]).map((value) => ({
  value,
  label: UNIT_LABELS[value],
}));

export const ISSUE_STATUS_LABELS: Record<IssueStatus, string> = {
  ISSUED: 'Выдано',
  RETURNED: 'Возвращено',
};

export const ISSUE_STATUS_COLORS: Record<IssueStatus, string> = {
  ISSUED: 'gold',
  RETURNED: 'green',
};

export const ISSUE_STATUS_OPTIONS = (
  Object.keys(ISSUE_STATUS_LABELS) as IssueStatus[]
).map((value) => ({ value, label: ISSUE_STATUS_LABELS[value] }));

export const RETURN_CONDITION_LABELS: Record<ReturnCondition, string> = {
  OK: 'В порядке',
  DAMAGED: 'Повреждено',
  LOST: 'Утеряно',
};

export const RETURN_CONDITION_COLORS: Record<ReturnCondition, string> = {
  OK: 'green',
  DAMAGED: 'orange',
  LOST: 'red',
};

export const RETURN_CONDITION_OPTIONS = (
  Object.keys(RETURN_CONDITION_LABELS) as ReturnCondition[]
).map((value) => ({ value, label: RETURN_CONDITION_LABELS[value] }));
