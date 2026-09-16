import type { ResidentKind } from '@/features/residents/types';

export const RESIDENT_KIND_LABELS: Record<ResidentKind, string> = {
  STUDENT: 'Студент',
  TEACHER: 'Преподаватель',
  STAFF_LIVING: 'Сотрудник',
};

export const RESIDENT_KIND_OPTIONS = (
  Object.keys(RESIDENT_KIND_LABELS) as ResidentKind[]
).map((value) => ({ value, label: RESIDENT_KIND_LABELS[value] }));
