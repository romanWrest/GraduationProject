import { apiClient } from '@/shared/api/client';
import type { AuthResponse, LoginCredentials, UserDto } from './types';

export async function login(credentials: LoginCredentials): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>('/auth/login', credentials);
  return data;
}

export async function refresh(refreshToken: string): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>('/auth/refresh', { refreshToken });
  return data;
}

export async function logout(refreshToken: string): Promise<void> {
  await apiClient.post('/auth/logout', { refreshToken });
}

export async function fetchMe(): Promise<UserDto> {
  const { data } = await apiClient.get<UserDto>('/users/me');
  return data;
}

export interface UpdateProfilePayload {
  fullName?: string;
  phone?: string;
  email?: string;
}

export async function updateMe(payload: UpdateProfilePayload): Promise<UserDto> {
  const { data } = await apiClient.put<UserDto>('/users/me', payload);
  return data;
}

export async function changePassword(payload: {
  oldPassword: string;
  newPassword: string;
}): Promise<void> {
  await apiClient.post('/auth/password-change', payload);
}
