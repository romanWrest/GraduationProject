import type { AxiosRequestConfig } from 'axios';
import axios from 'axios';
import { apiClient } from './client';
import { useAuthStore } from '@/features/auth/store';
import type { AuthResponse } from '@/features/auth/types';

interface RetriableConfig extends AxiosRequestConfig {
  _retry?: boolean;
}

let refreshPromise: Promise<string> | null = null;

async function performRefresh(): Promise<string> {
  const refreshToken = useAuthStore.getState().refreshToken;
  if (!refreshToken) {
    throw new Error('No refresh token');
  }
  const response = await axios.post<AuthResponse>(
    `${apiClient.defaults.baseURL}/auth/refresh`,
    { refreshToken },
    { headers: { 'Content-Type': 'application/json' } },
  );
  const { accessToken, refreshToken: newRefreshToken } = response.data;
  useAuthStore.getState().setTokens(accessToken, newRefreshToken);
  return accessToken;
}

export function installInterceptors(): void {
  apiClient.interceptors.request.use((config) => {
    const token = useAuthStore.getState().accessToken;
    if (token) {
      config.headers = config.headers ?? {};
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  });

  apiClient.interceptors.response.use(
    (response) => response,
    async (error) => {
      const original = error.config as RetriableConfig | undefined;
      const status = error.response?.status;
      const url: string = original?.url ?? '';
      const isAuthEndpoint =
        url.includes('/auth/login') ||
        url.includes('/auth/refresh') ||
        url.includes('/auth/logout');

      if (status === 401 && original && !original._retry && !isAuthEndpoint) {
        original._retry = true;
        try {
          refreshPromise ??= performRefresh().finally(() => {
            refreshPromise = null;
          });
          const newAccess = await refreshPromise;
          original.headers = original.headers ?? {};
          (original.headers as Record<string, string>).Authorization = `Bearer ${newAccess}`;
          return apiClient(original);
        } catch (refreshError) {
          useAuthStore.getState().logout();
          if (typeof window !== 'undefined' && window.location.pathname !== '/login') {
            window.location.href = '/login';
          }
          return Promise.reject(refreshError);
        }
      }

      return Promise.reject(error);
    },
  );
}
