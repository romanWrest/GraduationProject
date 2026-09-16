import type { RequestStatus } from '@/features/requests/types';

export const STATUS_COLORS: Record<RequestStatus, string> = {
  NEW: 'blue',
  IN_REVIEW: 'cyan',
  ASSIGNED: 'gold',
  IN_PROGRESS: 'orange',
  DONE: 'lime',
  CLOSED: 'green',
  REJECTED: 'red',
  CANCELLED: 'default',
};
