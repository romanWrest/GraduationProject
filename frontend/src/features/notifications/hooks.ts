import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  getSettings,
  listNotifications,
  markAllRead,
  markRead,
  unreadCount,
  updateSettings,
} from './api';
import type { NotificationsFilters, UpdateSettingsPayload } from './types';
import { useAuthStore } from '@/features/auth/store';

const KEY = ['notifications'] as const;

const POLL_INTERVAL =
  Number(import.meta.env.VITE_NOTIFICATIONS_POLL_INTERVAL_MS) || 30_000;

export function useNotifications(filters: NotificationsFilters) {
  return useQuery({
    queryKey: [...KEY, 'list', filters],
    queryFn: () => listNotifications(filters),
    placeholderData: (prev) => prev,
  });
}

export function useUnreadCount() {
  const accessToken = useAuthStore((s) => s.accessToken);
  return useQuery({
    queryKey: [...KEY, 'unread-count'],
    queryFn: unreadCount,
    refetchInterval: POLL_INTERVAL,
    refetchIntervalInBackground: false,
    enabled: !!accessToken,
  });
}

export function useMarkRead() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => markRead(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  });
}

export function useMarkAllRead() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: () => markAllRead(),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  });
}

export function useSettings() {
  return useQuery({
    queryKey: [...KEY, 'settings'],
    queryFn: getSettings,
  });
}

export function useUpdateSettings() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (payload: UpdateSettingsPayload) => updateSettings(payload),
    onSuccess: () => qc.invalidateQueries({ queryKey: [...KEY, 'settings'] }),
  });
}
