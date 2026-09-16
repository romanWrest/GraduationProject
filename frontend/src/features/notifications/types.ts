export type NotificationType =
  | 'WELCOME'
  | 'ACCOUNT_DEACTIVATED'
  | 'PASSWORD_RESET'
  | 'RESIDENT_ENROLLED'
  | 'RESIDENT_EVICTED'
  | 'RESIDENT_MOVED'
  | 'REQUEST_CREATED'
  | 'REQUEST_ASSIGNED'
  | 'REQUEST_STATUS_CHANGED'
  | 'REQUEST_CLOSED'
  | 'SYSTEM';

export interface NotificationDto {
  id: string;
  userId: string;
  type: NotificationType;
  title: string;
  body: string;
  payload?: Record<string, unknown>;
  read: boolean;
  readAt?: string;
  createdAt: string;
}

export interface NotificationSettingsDto {
  emailEnabled: boolean;
  inAppEnabled: boolean;
  byType: Record<string, Record<string, boolean>>;
}

export interface UpdateSettingsPayload {
  emailEnabled?: boolean;
  inAppEnabled?: boolean;
  byType?: Record<string, Record<string, boolean>>;
}

export interface NotificationsFilters {
  unreadOnly?: boolean;
  type?: NotificationType;
  from?: string;
  to?: string;
  page?: number;
  size?: number;
}
