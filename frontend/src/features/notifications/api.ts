import { apiClient } from '@/shared/api/client';
import type { Page } from '@/shared/api/types';
import type {
  NotificationDto,
  NotificationSettingsDto,
  NotificationsFilters,
  UpdateSettingsPayload,
} from './types';

export async function listNotifications(
  filters: NotificationsFilters,
): Promise<Page<NotificationDto>> {
  const { data } = await apiClient.get<Page<NotificationDto>>('/notifications', {
    params: filters,
  });
  return data;
}

export async function unreadCount(): Promise<{ count: number }> {
  const { data } = await apiClient.get<{ count: number }>('/notifications/unread-count');
  return data;
}

export async function markRead(id: string): Promise<void> {
  await apiClient.patch(`/notifications/${id}/read`);
}

export async function markAllRead(): Promise<{ affected: number }> {
  const { data } = await apiClient.patch<{ affected: number }>('/notifications/read-all');
  return data;
}

export async function getSettings(): Promise<NotificationSettingsDto> {
  const { data } = await apiClient.get<NotificationSettingsDto>('/notifications/settings');
  return data;
}

export async function updateSettings(
  payload: UpdateSettingsPayload,
): Promise<NotificationSettingsDto> {
  const { data } = await apiClient.patch<NotificationSettingsDto>(
    '/notifications/settings',
    payload,
  );
  return data;
}
