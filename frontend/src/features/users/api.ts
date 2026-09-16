import { apiClient } from '@/shared/api/client';
import type { Page } from '@/shared/api/types';
import type {
  CreatedUserDto,
  CreateUserPayload,
  UpdateUserPayload,
  UserDto,
  UsersFilters,
} from './types';
import type { Role } from '@/shared/constants/roles';

export async function listUsers(filters: UsersFilters): Promise<Page<UserDto>> {
  const { data } = await apiClient.get<Page<UserDto>>('/users', { params: filters });
  return data;
}

export async function getUser(id: string): Promise<UserDto> {
  const { data } = await apiClient.get<UserDto>(`/users/${id}`);
  return data;
}

export async function createUser(payload: CreateUserPayload): Promise<CreatedUserDto> {
  const { data } = await apiClient.post<CreatedUserDto>('/users', payload);
  return data;
}

export async function updateUser(id: string, payload: UpdateUserPayload): Promise<UserDto> {
  const { data } = await apiClient.put<UserDto>(`/users/${id}`, payload);
  return data;
}

export async function setUserStatus(id: string, active: boolean): Promise<UserDto> {
  const { data } = await apiClient.put<UserDto>(`/users/${id}/status`, { active });
  return data;
}

export async function setUserRoles(id: string, roles: Role[]): Promise<UserDto> {
  const { data } = await apiClient.put<UserDto>(`/users/${id}/roles`, { roles });
  return data;
}

export async function deactivateUser(id: string): Promise<void> {
  await apiClient.delete(`/users/${id}`);
}
